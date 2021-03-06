package com.rbkmoney.fistful.reporter.poller.impl;

import com.rbkmoney.dao.DaoException;
import com.rbkmoney.fistful.identity.*;
import com.rbkmoney.fistful.reporter.dao.ChallengeDao;
import com.rbkmoney.fistful.reporter.dao.IdentityDao;
import com.rbkmoney.fistful.reporter.domain.enums.ChallengeEventType;
import com.rbkmoney.fistful.reporter.domain.enums.IdentityEventType;
import com.rbkmoney.fistful.reporter.domain.tables.pojos.Identity;
import com.rbkmoney.fistful.reporter.exception.StorageException;
import com.rbkmoney.fistful.reporter.poller.IdentityEventHandler;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class IdentityChallengeStatusChangedHandler implements IdentityEventHandler {

    private final ChallengeDao challengeDao;
    private final IdentityDao identityDao;

    @Override
    public boolean accept(Change change) {
        return change.isSetIdentityChallenge()
                && change.getIdentityChallenge().isSetPayload()
                && change.getIdentityChallenge().getPayload().isSetStatusChanged();
    }

    @Override
    public void handle(Change change, SinkEvent event) {
        try {
            ChallengeChange challengeChange = change.getIdentityChallenge();
            ChallengeStatus status = challengeChange.getPayload().getStatusChanged();
            log.info("Start identity challenge status changed handling, eventId={}, identityId={}, challengeId={}, status={}", event.getId(), event.getSource(), challengeChange.getId(), status);
            updateChallenge(event, challengeChange, status);

            log.info("Challenge status changed handling: update identity, eventId={}, identityId={}", event.getId(), event.getSource());
            updateIdentity(event);
            log.info("Challenge status changed handling: identity have been updated, eventId={}, identityId={}", event.getId(), event.getSource());

            log.info("Identity challenge status have been changed, eventId={}, identityId={}, challengeId={}, status={}", event.getId(), event.getSource(), challengeChange.getId(), status);
        } catch (DaoException e) {
            throw new StorageException(e);
        }
    }

    private void updateChallenge(SinkEvent event, ChallengeChange challengeChange, ChallengeStatus status) {
        com.rbkmoney.fistful.reporter.domain.tables.pojos.Challenge challenge = challengeDao.get(event.getSource(), challengeChange.getId());

        challenge.setId(null);
        challenge.setWtime(null);

        challenge.setEventId(event.getId());
        challenge.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        challenge.setIdentityId(event.getSource());
        challenge.setSequenceId(event.getPayload().getSequence());
        challenge.setEventOccuredAt(TypeUtil.stringToLocalDateTime(event.getPayload().getOccuredAt()));
        challenge.setEventType(ChallengeEventType.CHALLENGE_STATUS_CHANGED);
        challenge.setChallengeId(challengeChange.getId());
        challenge.setChallengeStatus(TBaseUtil.unionFieldToEnum(status, com.rbkmoney.fistful.reporter.domain.enums.ChallengeStatus.class));
        if (status.isSetCompleted()) {
            ChallengeCompleted challengeCompleted = status.getCompleted();
            challenge.setChallengeResolution(TypeUtil.toEnumField(challengeCompleted.getResolution().toString(), com.rbkmoney.fistful.reporter.domain.enums.ChallengeResolution.class));
            if (challengeCompleted.isSetValidUntil()) {
                challenge.setChallengeValidUntil(TypeUtil.stringToLocalDateTime(challengeCompleted.getValidUntil()));
            }
        }

        challengeDao.updateNotCurrent(event.getSource(), challengeChange.getId());
        challengeDao.save(challenge);
    }

    private void updateIdentity(SinkEvent event) {
        Identity identity = identityDao.get(event.getSource());

        identity.setId(null);
        identity.setWtime(null);

        identity.setEventId(event.getId());
        identity.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        identity.setIdentityId(event.getSource());
        identity.setSequenceId(event.getPayload().getSequence());
        identity.setEventOccuredAt(TypeUtil.stringToLocalDateTime(event.getPayload().getOccuredAt()));
        identity.setEventType(IdentityEventType.IDENTITY_CHALLENGE_STATUS_CHANGED);

        identityDao.updateNotCurrent(event.getSource());
        identityDao.save(identity);
    }
}
