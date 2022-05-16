package com.jihye.fc.finalproject.api.service;

import com.jihye.fc.finalproject.api.dto.AuthUser;
import com.jihye.fc.finalproject.api.dto.CreateShareReq;
import com.jihye.fc.finalproject.core.domain.RequestStatus;
import com.jihye.fc.finalproject.core.domain.entity.RequestReplyType;
import com.jihye.fc.finalproject.core.domain.entity.Share;
import com.jihye.fc.finalproject.core.domain.entity.User;
import com.jihye.fc.finalproject.core.domain.entity.repository.ShareRepository;
import com.jihye.fc.finalproject.core.exception.CalendarException;
import com.jihye.fc.finalproject.core.exception.ErrorCode;
import com.jihye.fc.finalproject.core.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ShareService {
	
	private final UserService userService;
	private final ShareRepository shareRepository;
	private final EmailService emailService;
	
	@Transactional
	public void createShare(AuthUser authUser, CreateShareReq req) {
		final User fromUser = userService.findByUserId(authUser.getId());
		final User toUser = userService.findByUserId(req.getToUserId());
		shareRepository.save(Share.builder()
			.fromUserId(fromUser.getId())
			.toUserId(toUser.getId())
			.direction(req.getDirection())
			.requestStatus(RequestStatus.REQUESTED)
		    .build());
		emailService.sendShareRequestMail(toUser.getEmail(), fromUser.getName(), req.getDirection());
	}
	
	@Transactional
	public void replyToShareRequest(Long shareId, AuthUser toAuthUser, RequestReplyType type) {
		shareRepository.findById(shareId)
				  .filter(share -> share.getToUserId().equals(toAuthUser.getId()))
				  .filter(share -> share.getRequestStatus() == RequestStatus.REQUESTED)
				  .map(share -> share.reply(type))
				  .orElseThrow(() -> new CalendarException(ErrorCode.BAD_REQUEST));
	}
	
	/**
	 * 공유 대상 조회
	 * 
	 * 자신과 양방향 공유관계인 상대방 (자신이 To, From  둘 다 가능)
	 * 내가 공유관계의 수신자(toUser) 인 경우 & 단방향
	 * @param authUser
	 * @return
	 */
	public List<Long> findSharedUserIdsByUser(AuthUser authUser) {
		final Stream<Long> biDirectionShares = shareRepository.findAllByDirection(
			    authUser.getId(),
			    RequestStatus.ACCEPTED,
			    Share.Direction.BI_DIRECTION
		).stream().map(s -> s.getToUserId().equals(authUser.getId()) ? s.getFromUserId() : s.getToUserId());
		
		final Stream<Long> uniDirectionShares = shareRepository.findAllByToUserIdAndRequestStatusAndDirection(
			    authUser.getId(),
			    RequestStatus.ACCEPTED,
			    Share.Direction.UNI_DIRECTION).stream().map(s -> s.getFromUserId());
		return Stream.concat(biDirectionShares, uniDirectionShares).collect(Collectors.toList());
	}
}
