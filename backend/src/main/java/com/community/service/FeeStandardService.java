package com.community.service;

import com.community.dto.FeeStandardDTO;
import com.community.entity.FeeStandard;

import java.util.List;

public interface FeeStandardService {

    List<FeeStandard> list();

    void updateAll(List<FeeStandardDTO> standards);
}
