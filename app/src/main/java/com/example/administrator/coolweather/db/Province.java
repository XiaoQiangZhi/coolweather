package com.example.administrator.coolweather.db;


import org.litepal.crud.DataSupport;

//id,provinceName,provinceCode
public class Province extends DataSupport{

    private int id;
    private String provinceName;
    private int provinceCode;//省代号

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public int getProvinceCode() {
        return provinceCode;
    }


}
