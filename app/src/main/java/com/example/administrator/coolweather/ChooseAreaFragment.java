package com.example.administrator.coolweather;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.coolweather.db.City;
import com.example.administrator.coolweather.db.Country;
import com.example.administrator.coolweather.db.Province;
import com.example.administrator.coolweather.util.HttpUtil;
import com.example.administrator.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class ChooseAreaFragment extends Fragment {
    //标记选择的是省，市，县
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTRY = 2;

    //private ProgressDialog progressDialog;
    private TextView textTitle;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    //省列表
    private List<Province> provinceList;
    //市列表
    private List<City> cityList;
    //县列表
    private List<Country> countryList;

    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);

        textTitle = view.findViewById(R.id.title_text);
        backButton = view.findViewById(R.id.back_button);
        listView = view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);

        return view;
    }

    //设置按钮和ListView的点击监听事件
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(i);
                    Log.d("Check", String.valueOf(selectedProvince.getProvinceCode())); //0
                    Log.d("Check", String.valueOf(selectedProvince.getId()));
                    Log.d("Check", String.valueOf(selectedProvince.getProvinceName()));
                    queryCities();//加载相应城市的方法
                }
                else if (currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(i);
                    queryCountries();
                }
                else if (currentLevel == LEVEL_COUNTRY){
                    String weatherId = countryList.get(i).getWeather_id();
                    Intent intent = new Intent(getActivity(),WeatherActivity.class);
                    intent.putExtra("weather_id",weatherId);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel == LEVEL_COUNTRY){
                    queryCities();//加载相应市的方法
                }
                if (currentLevel == LEVEL_CITY){
                    queryProvinces();//加载相应省的方法
                }
            }
        });
        //加载省级数据
        queryProvinces();
    }

    /**
     * 查询全国的省，有限从数据库查找，如果数据库没有，再从服务器上查询
     */
    private void queryProvinces(){
        textTitle.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else{
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }

    /**
     * 查询选中的省的所有市，优先从数据库查找，如果没有，再从服务器上查询
     */
    private void queryCities(){
        textTitle.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("id = ?",String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size() > 0){
            dataList.clear();
            for (City city : cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else{
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/"+provinceCode;
            Log.d("Check",address);
            queryFromServer(address,"city");
        }

    }

    /**
     * 查询选中的市里的所有县，优先从数据库中查询，如果没有再从服务器上查询
     */
    private void queryCountries(){
        textTitle.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countryList = DataSupport.where("id = ?",String.valueOf(selectedCity.getId())).find(Country.class);
        if (countryList.size() >0 ){
            dataList.clear();
            for (Country country: countryList){
                dataList.add(country.getCountryName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTRY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            Log.d("Check",address);
            queryFromServer(address,"country");
        }
    }

    /**
     *根据传入的地址和类型从服务器上查询省市县的的数据
     */
    private void queryFromServer(String address,final String type){
        //showProgressDiifalog();
        HttpUtil.sendOkHttpResquest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responceText = response.body().string();
                boolean result = false;
                if ("province".equals(type)){
                    result = Utility.handleProvinceResponse(responceText);
                }else if ("city".equals(type)){
                    result = Utility.handleCityResponse(responceText,selectedProvince.getId());
                }else if ("country".equals(type)){
                    result = Utility.handleCountryResponse(responceText,selectedCity.getId());
                }
                if (result){
                    //回到主线程更新UI
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            }
                            else if ("city".equals(type)){
                                queryCities();
                            }else if ("country".equals(type)){
                                queryCountries();
                            }
                        }
                    });
                }
            }
        });
    }

//    //显示进度对话框
//    private void showProgressDiifalog(){
//        if (progressDialog == null){
//            progressDialog = new ProgressDialog(getActivity());
//            progressDialog.setMessage("正在加载...");
//            progressDialog.setCanceledOnTouchOutside(false);
//        }
//        progressDialog.show();
//    }
//
//    //关闭进度对话框
//    private void closeProgressDialog(){
//        if (progressDialog != null){
//            progressDialog.dismiss();
//        }
//    }
}
