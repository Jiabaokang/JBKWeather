package com.jbkweather.android.views;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jbkweather.android.MainActivity;
import com.jbkweather.android.R;
import com.jbkweather.android.WeatherActivity;
import com.jbkweather.android.appbase.WeatherApplication;
import com.jbkweather.android.database.City;
import com.jbkweather.android.database.County;
import com.jbkweather.android.database.Province;
import com.jbkweather.android.util.Constant;
import com.jbkweather.android.util.HttpUtil;
import com.jbkweather.android.util.LogUtil;
import com.jbkweather.android.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class ChooseAreaFragment extends Fragment implements View.OnClickListener,
        AdapterView.OnItemClickListener{

    public static final String TAG = ChooseAreaFragment.class.getName();

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;

    private TextView titleText;

    private Button backButton;

    private ListView listView;

    private ArrayAdapter<String> adapter;

    private List<String> dataList = new ArrayList<>();

    //省列表
    private List<Province> provinceList;

    //市列表
    private List<City> cityList;

    //县列表
    private List<County> countyList;

    //选中的省份
    private Province selectedProvince;

    //选中的城市
    private City selectedCity;

    //选中的县区
    private County selectedCounty;

    //当前选中的级别
    private int currentLevel;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button)view.findViewById(R.id.back_button);
        listView = (ListView)view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        backButton.setOnClickListener(this);
        listView.setOnItemClickListener(this);
        queryProvince();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back_button:
                if(currentLevel == LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    queryProvince();
                }
                break;
            default:
        }
    }

    /**
     * item的点击事件处理
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(currentLevel == LEVEL_PROVINCE){
            selectedProvince = provinceList.get(position);
            queryCities();
        }else if (currentLevel == LEVEL_CITY){
            selectedCity = cityList.get(position);
            queryCountys();
        }else if(currentLevel == LEVEL_COUNTY){

            String weatherId = countyList.get(position).getWeathreId();
            if (getActivity() instanceof MainActivity) {
                Intent intent = new Intent(getActivity(), WeatherActivity.class);
                intent.putExtra("weather_id", weatherId);
                startActivity(intent);
                getActivity().finish();
            }else if (getActivity() instanceof WeatherActivity) {
                WeatherActivity activity = (WeatherActivity) getActivity();
//                activity.drawerLayout.closeDrawers();
//                activity.swipeRefresh.setRefreshing(true);
                activity.requestWeather(weatherId);
            }
        }
    }

    /**
     * 查询全国所有的省份，优先在数据库中查找，如果没有再到服务器去查
     */
    private void queryProvince() {
        titleText.setText(R.string.china);
        backButton.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if(provinceList.size() > 0){
            dataList.clear();
            for (Province province:provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else{
            queryFromServer(Constant.BASE_URL,Constant.PROVINCE);
        }

    }

    /**
     * 查询选中的省份对应的所有城市，优先在数据库中查找，如果没有再到服务器去查
     */
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?",String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size() > 0){
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else{
            int provinceCode = selectedProvince.getProvinceCode();
            StringBuffer address = new StringBuffer();
            address.append(Constant.BASE_URL).append(provinceCode);
            queryFromServer(address.toString(),Constant.CITY);
        }
    }

    /**
     * 查询选中的城市对应的所有的县区，优先在数据库中查找，如果没有再到服务器去查
     */
    private void queryCountys() {

        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList  = DataSupport.where("cityid = ?",String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size() > 0){
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else{
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();

            StringBuffer address = new StringBuffer();
            address.append(Constant.BASE_URL).append(provinceCode).append("/").append(cityCode);
            //请求县区数据
           queryFromServer(address.toString(),Constant.COUNTY);
        }
    }

    /**
     * 根据传入的地址和类型从服务器查询省市县数据
     */
    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                LogUtil.d(TAG,"responseText=="+responseText);
                boolean result = false;
                if(Constant.PROVINCE.equals(type)){
                    result = Utility.handleProvinceResponse(responseText);
                }else if(Constant.CITY.equals(type)){
                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if(Constant.COUNTY.equals(type)){
                    result = Utility.handleCountyResponse(responseText,selectedCity.getId());
                }

                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if(Constant.PROVINCE.equals(type)){
                                queryProvince();
                            }else if(Constant.CITY.equals(type)){
                                queryCities();
                            }else if(Constant.COUNTY.equals(type)){
                                queryCountys();
                            }
                        }
                    });

                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(WeatherApplication.getContext(),
                                getString(R.string.load_failed),Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });

    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if(progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getString(R.string.progressDialogMessage));
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog(){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }

}
