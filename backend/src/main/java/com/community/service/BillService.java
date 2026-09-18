package com.community.service;

import com.community.common.PageVO;
import com.community.dto.BillExcelVO;
import com.community.dto.BillGenerateDTO;
import com.community.dto.BillGenerateVO;
import com.community.dto.BillVO;
import com.community.entity.Bill;

import java.util.List;

public interface BillService {

    BillGenerateVO generate(BillGenerateDTO dto, Long adminId);

    PageVO<BillVO> pageForAdmin(int page, int size, Integer status, String building, String month);

    List<BillExcelVO> listForExport(Integer status, String building, String month);

    void delete(Long id);

    PageVO<Bill> pageForOwner(Long userId, int page, int size, Integer status);

    void pay(Long billId, Long userId);

    int generateMonthlyBills();

    int remindUnpaidOwners();
}
