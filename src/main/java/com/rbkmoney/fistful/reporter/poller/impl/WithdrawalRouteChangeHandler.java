package com.rbkmoney.fistful.reporter.poller.impl;

import com.rbkmoney.dao.DaoException;
import com.rbkmoney.fistful.reporter.dao.FistfulCashFlowDao;
import com.rbkmoney.fistful.reporter.dao.WithdrawalDao;
import com.rbkmoney.fistful.reporter.domain.enums.FistfulCashFlowChangeType;
import com.rbkmoney.fistful.reporter.domain.enums.WithdrawalEventType;
import com.rbkmoney.fistful.reporter.domain.tables.pojos.FistfulCashFlow;
import com.rbkmoney.fistful.reporter.domain.tables.pojos.Withdrawal;
import com.rbkmoney.fistful.reporter.exception.StorageException;
import com.rbkmoney.fistful.reporter.poller.WithdrawalEventHandler;
import com.rbkmoney.fistful.withdrawal.Change;
import com.rbkmoney.fistful.withdrawal.SinkEvent;
import com.rbkmoney.geck.common.util.TypeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class WithdrawalRouteChangeHandler implements WithdrawalEventHandler {

    private final WithdrawalDao withdrawalDao;
    private final FistfulCashFlowDao fistfulCashFlowDao;

    @Override
    public boolean accept(Change change) {
        return change.isSetRoute() && change.getRoute().isSetRoute();
    }

    @Override
    public void handle(Change change, SinkEvent event) {
        try {
            String providerId = change.getRoute().getRoute().getProviderId();

            log.info("Start withdrawal provider id changed handling, eventId={}, walletId={}, providerId={}", event.getId(), event.getSource(), providerId);
            Withdrawal withdrawal = withdrawalDao.get(event.getSource());

            withdrawal.setId(null);
            withdrawal.setWtime(null);

            withdrawal.setEventId(event.getId());
            withdrawal.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
            withdrawal.setWithdrawalId(event.getSource());
            withdrawal.setSequenceId(event.getPayload().getSequence());
            withdrawal.setEventOccuredAt(TypeUtil.stringToLocalDateTime(event.getPayload().getOccuredAt()));
            withdrawal.setEventType(WithdrawalEventType.WITHDRAWAL_ROUTE_CHANGED);
            withdrawal.setProviderId(providerId);

            withdrawalDao.updateNotCurrent(event.getSource());
            long id = withdrawalDao.save(withdrawal);

            List<FistfulCashFlow> cashFlows = fistfulCashFlowDao.getByObjId(withdrawal.getId(), FistfulCashFlowChangeType.withdrawal);
            fillCashFlows(cashFlows, event, WithdrawalEventType.WITHDRAWAL_ROUTE_CHANGED, id);

            fistfulCashFlowDao.save(cashFlows);
            log.info("Withdrawal provider id have been changed, eventId={}, walletId={}, providerId={}", event.getId(), event.getSource(), providerId);
        } catch (DaoException e) {
            throw new StorageException(e);
        }
    }
}
