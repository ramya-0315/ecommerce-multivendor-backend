package com.ramyastore.service;

import com.ramyastore.model.Home;
import com.ramyastore.model.HomeCategory;

import java.util.List;

public interface HomeService {

    Home creatHomePageData(List<HomeCategory> categories);

}
