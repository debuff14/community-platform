package com.community.scheduler;

import com.community.service.BillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillScheduler {

    private final BillService billService;

    @Scheduled(cron = "${community.bill.cron.generate}")
    public void generateMonthlyBills() {
        int count = billService.generateMonthlyBills();
        log.info("每月账单生成任务完成，新增 {} 条", count);
    }

    @Scheduled(cron = "${community.bill.cron.remind}")
    public void remindUnpaidOwners() {
        int count = billService.remindUnpaidOwners();
        log.info("缴费提醒任务完成，提醒 {} 人", count);
    }
}
