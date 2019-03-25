package com.example.zj.cool.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {

    //@SerializedName（""）注解：
    // Gson解析的时候就会将json对应的值赋值到对应属性上
    // -后面不需要分号
    @SerializedName("location")
    public String cityName;

    @SerializedName("cid")
    public String weatherId;


}
