package com.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.common.PageVO;
import com.community.common.exception.BusinessException;
import com.community.dto.BillExcelVO;
import com.community.dto.BillGenerateDTO;
import com.community.dto.BillGenerateVO;
import com.community.dto.BillVO;
import com.community.entity.Bill;
import com.community.entity.FeeStandard;
import com.community.entity.Message;
import com.community.entity.PaymentRecord;
import com.community.entity.User;
import com.community.mapper.BillMapper;
import com.community.mapper.FeeStandardMapper;
import com.community.mapper.MessageMapper;
import com.community.mapper.PaymentRecordMapper;
import com.community.mapper.UserMapper;
import com.community.service.BillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private static final Map<Integer, String> FEE_TYPE_NAMES = Map.of(1, "物业费", 2, "停车费", 3, "水电费");
    private static final DateTimeFormatter PAY_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MAX_PAGE_SIZE = 50;

    private final BillMapper billMapper;
    private final UserMapper userMapper;
    private final FeeStandardMapper feeStandardMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final MessageMapper messageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillGenerateVO generate(BillGenerateDTO dto, Long adminId) {
        List<User> owners = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getRole, 0)
                .eq(StringUtils.hasText(dto.getBuilding()), User::getBuilding, dto.getBuilding()));
        int generated = 0;
        int skipped = 0;
        for (User owner : owners) {
            boolean exists = billMapper.selectCount(new LambdaQueryWrapper<Bill>()
                    .eq(Bill::getUserId, owner.getId())
                    .eq(Bill::getMonth, dto.getMonth())
                    .eq(Bill::getFeeType, dto.getFeeType())) > 0;
            if (exists) {
                skipped++;
                continue;
            }
            Bill bill = new Bill();
            bill.setUserId(owner.getId());
            bill.setFeeType(dto.getFeeType());
            bill.setAmount(dto.getAmount());
            bill.setMonth(dto.getMonth());
            bill.setStatus(0);
            try {
                billMapper.insert(bill);
                generated++;
            } catch (DuplicateKeyException e) {
                skipped++;
            }
        }
        return new BillGenerateVO(generated, skipped);
    }

    @Override
    public PageVO<BillVO> pageForAdmin(int page, int size, Integer status, String building, String month) {
        int[] normalized = normalize(page, size);
        Page<BillVO> dbPage = new Page<>(normalized[0], normalized[1]);
        return PageVO.of(billMapper.selectAdminPage(dbPage, status, building, month));
    }

    @Override
    public List<BillExcelVO> listForExport(Integer status, String building, String month) {
        List<BillVO> list = billMapper.selectAdminList(status, building, month);
        List<BillExcelVO> result = new ArrayList<>(list.size());
        for (BillVO bill : list) {
            BillExcelVO excel = new BillExcelVO();
            excel.setUsername(bill.getUsername());
            excel.setAddress((bill.getBuilding() == null ? "" : bill.getBuilding()) + (bill.getRoomNo() == null ? "" : bill.getRoomNo()));
            excel.setFeeTypeName(FEE_TYPE_NAMES.getOrDefault(bill.getFeeType(), "其他"));
            excel.setAmount(bill.getAmount());
            excel.setMonth(bill.getMonth());
            excel.setStatusName(bill.getStatus() != null && bill.getStatus() == 1 ? "已支付" : "未支付");
            excel.setPayTime(bill.getPayTime() == null ? "" : bill.getPayTime().format(PAY_TIME_FORMATTER));
            result.add(excel);
        }
        return result;
    }

    @Override
    public void delete(Long id) {
        Bill bill = billMapper.selectById(id);
        if (bill == null) {
            throw new BusinessException("账单不存在");
        }
        if (bill.getStatus() != null && bill.getStatus() == 1) {
            throw new BusinessException("已支付的账单不允许删除");
        }
        billMapper.deleteById(id);
    }

    @Override
    public PageVO<Bill> pageForOwner(Long userId, int page, int size, Integer status) {
        int[] normalized = normalize(page, size);
        Page<Bill> dbPage = billMapper.selectPage(new Page<>(normalized[0], normalized[1]),
                new LambdaQueryWrapper<Bill>()
                        .eq(Bill::getUserId, userId)
                        .eq(status != null, Bill::getStatus, status)
                        .orderByDesc(Bill::getMonth)
                        .orderByDesc(Bill::getId));
        return PageVO.of(dbPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(Long billId, Long userId) {
        Bill bill = billMapper.selectById(billId);
        if (bill == null) {
            throw new BusinessException("账单不存在");
        }
        if (!bill.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权支付他人的账单");
        }
        if (bill.getStatus() != null && bill.getStatus() == 1) {
            throw new BusinessException("该账单已支付");
        }
        LocalDateTime payTime = LocalDateTime.now();
        int updated = billMapper.updateStatusToPaid(billId, payTime);
        if (updated == 0) {
            throw new BusinessException("该账单已支付");
        }
        PaymentRecord record = new PaymentRecord();
        record.setPayNo(generatePayNo());
        record.setBillId(billId);
        record.setUserId(userId);
        record.setAmount(bill.getAmount());
        record.setPayTime(payTime);
        try {
            paymentRecordMapper.insert(record);
        } catch (DuplicateKeyException e) {
            record.setPayNo(generatePayNo());
            paymentRecordMapper.insert(record);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int generateMonthlyBills() {
        String month = YearMonth.now().toString();
        FeeStandard standard = feeStandardMapper.selectOne(new LambdaQueryWrapper<FeeStandard>()
                .eq(FeeStandard::getFeeType, 1));
        if (standard == null) {
            log.warn("未配置物业费收费标准，跳过本月账单生成");
            return 0;
        }
        List<User> owners = userMapper.selectList(new LambdaQueryWrapper<User>().eq(User::getRole, 0));
        int generated = 0;
        for (User owner : owners) {
            boolean exists = billMapper.selectCount(new LambdaQueryWrapper<Bill>()
                    .eq(Bill::getUserId, owner.getId())
                    .eq(Bill::getMonth, month)
                    .eq(Bill::getFeeType, 1)) > 0;
            if (exists) {
                continue;
            }
            Bill bill = new Bill();
            bill.setUserId(owner.getId());
            bill.setFeeType(1);
            bill.setAmount(standard.getAmount());
            bill.setMonth(month);
            bill.setStatus(0);
            try {
                billMapper.insert(bill);
            } catch (DuplicateKeyException e) {
                continue;
            }
            generated++;
            saveMessage(owner.getId(), "您有新的 " + month + " 物业费账单待支付", bill.getId());
        }
        log.info("定时任务生成 {} 年 {} 月物业费账单 {} 条", month, generated);
        return generated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int remindUnpaidOwners() {
        String month = YearMonth.now().toString();
        List<Bill> unpaidBills = billMapper.selectList(new LambdaQueryWrapper<Bill>()
                .eq(Bill::getStatus, 0)
                .eq(Bill::getMonth, month));
        Map<Long, Long> userFirstBill = unpaidBills.stream().collect(Collectors.toMap(
                Bill::getUserId, Bill::getId, (a, b) -> a));
        for (Map.Entry<Long, Long> entry : userFirstBill.entrySet()) {
            saveMessage(entry.getKey(), "您的物业费明天截止，请及时支付", entry.getValue());
        }
        log.info("定时任务发送缴费提醒 {} 条", userFirstBill.size());
        return userFirstBill.size();
    }

    private void saveMessage(Long userId, String content, Long relatedId) {
        Message message = new Message();
        message.setUserId(userId);
        message.setContent(content);
        message.setType(2);
        message.setRelatedId(relatedId);
        message.setIsRead(0);
        messageMapper.insert(message);
    }

    private String generatePayNo() {
        return "PAY" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + ThreadLocalRandom.current().nextInt(100000, 1000000);
    }

    private int[] normalize(int page, int size) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            size = 10;
        }
        return new int[]{page, size};
    }
}
