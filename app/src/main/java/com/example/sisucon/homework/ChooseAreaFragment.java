package com.example.sisucon.homework;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.SharedElementCallback;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.Fragment;
import com.example.sisucon.homework.util.Utility;
import com.example.sisucon.homework.util.Utils;
import com.example.sisucon.homework.weatherDB.City;
import com.example.sisucon.homework.weatherDB.Country;
import com.example.sisucon.homework.weatherDB.Province;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    private static final String TAG = "ChooseAreaFragment";

    public static final int LEVEL_PROVINCE = 0;

    public static final int LEVEL_CITY = 1;

    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;

    private TextView titleText;

    private Button backButton;

    private ListView listView;

    private ArrayAdapter<String> adapter;

    private List<String> dataList = new ArrayList<>();

    /**
     * 省列表
     */
    private List<Province> provinceList;

    /**
     * 市列表
     */
    private List<City> cityList;

    /**
     * 县列表
     */
    private List<Country> countyList;

    /**
     * 选中的省份
     */
    private Province selectedProvince;

    /**
     * 选中的城市
     */
    private City selectedCity;

    private String weaterCode;

    /**
     * 当前选中的级别
     */
    private int currentLevel;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        titleText.setText("sisucon");
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK)
                {
                    if (currentLevel == LEVEL_CITY) {
                        System.out.println("currentLevel = " + currentLevel);
                        queryProvinces();
                        return true;
                    }
                    else if (currentLevel == LEVEL_COUNTY) {
                        System.out.println("currentLevel = " + currentLevel);
                        queryCities();
                        return true;
                    }
                    return true;
                }
                return false;
            }
        });
        return view;
    }

    @Override
    public void setEnterSharedElementCallback(SharedElementCallback callback) {
        super.setEnterSharedElementCallback(callback);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                }
                else if(currentLevel == LEVEL_PROVINCE)
                {
                getActivity().finish();
                }
            }
        });



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    System.out.println(selectedProvince.getName());
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();
                }
                else
                {
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefresh.setRefreshing(true);
                        weaterCode = countyList.get(position).getWeatherID();
                        activity.requestWeather(weaterCode);
                    }

            }
        });
        queryProvinces();
    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryProvinces() {
        try
        {
            titleText.setText("中国");
            if (Province.class!=null)
            provinceList = DataSupport.findAll(Province.class);
            if (provinceList.size()>0) {
                dataList.clear();
                for (Province province : provinceList) {
                    dataList.add(province.getName());
                }
                adapter.notifyDataSetChanged();
                listView.setSelection(0);
                currentLevel = LEVEL_PROVINCE;
            } else {
                String address = "http://guolin.tech/api/china";
                queryFromServer(address, "province");
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryCities() {
        titleText.setText(selectedProvince.getName());
        if (DataSupport.findAll(City.class).size()>0) {
            System.out.println("DataCity: " + DataSupport.findAll(City.class).size());
            cityList = DataSupport.where("provinceCode = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        }
        if (cityList!=null&& cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    /**
     * 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryCounties() {
       try
       {
           titleText.setText(selectedCity.getCityName());
           System.out.println(DataSupport.findAll(Country.class).size());
           if (DataSupport.findAll(Country.class).size()>0){
               System.out.println(DataSupport.findAll(Country.class).size());
               countyList = DataSupport.where("cityID = ?", String.valueOf(selectedCity.getId())).find(Country.class);
           }
           if (countyList!=null&& countyList.size() > 0) {
               dataList.clear();
               for (Country country : countyList) {
                   dataList.add(country.getCountryName());
               }
               adapter.notifyDataSetChanged();
               listView.setSelection(0);
               currentLevel = LEVEL_COUNTY;
           } else {
               int cityCode = selectedCity.getCityCode();
               String address = "http://guolin.tech/api/china/" +selectedProvince.getProvinceCode()+"/"+cityCode;
               System.out.println(address);
               queryFromServer(address, "county");
           }
       }catch (Exception e)
       {
           e.printStackTrace();
       }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据。
     */
    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        Utils.seedMessage(address, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                // 通过runOnUiThread()方法回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

public void backLastView()
{
    if (currentLevel == LEVEL_COUNTY) {
        queryCities();
    } else if (currentLevel == LEVEL_CITY) {
        queryProvinces();
    }
}

}
