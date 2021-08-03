package com.solver.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.solver.api.request.SolverGetListReq;
import com.solver.api.request.UserRegistPostReq;
import com.solver.api.response.SolverRes;
import com.solver.common.auth.KakaoUtil;
import com.solver.common.model.OAuthToken;
import com.solver.common.util.RandomIdUtil;
import com.solver.db.entity.answer.Evaluation;
import com.solver.db.entity.code.Category;
import com.solver.db.entity.code.Code;
import com.solver.db.entity.code.FavoriteField;
import com.solver.db.entity.code.PointCode;
import com.solver.db.entity.user.PointLog;
import com.solver.db.entity.user.Token;
import com.solver.db.entity.user.TokenId;
import com.solver.db.entity.user.User;
import com.solver.db.entity.user.UserCalendar;
import com.solver.db.repository.answer.EvaluationRepository;
import com.solver.db.repository.code.CategoryRepository;
import com.solver.db.repository.code.CodeRepository;
import com.solver.db.repository.code.FavoriteFieldRepository;
import com.solver.db.repository.code.PointCodeRepository;
import com.solver.db.repository.group.GroupInfoRepository;
import com.solver.db.repository.group.GroupMemberRepository;
import com.solver.db.repository.user.PointLogRepository;
import com.solver.db.repository.user.TokenRepository;
import com.solver.db.repository.user.UserCalendarRepository;
import com.solver.db.repository.user.UserRepository;
import com.solver.db.repository.user.UserRepositorySupport;

/*userService랑 profileService 구분 짓는게 나을 듯*/
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository;

	@Autowired
	UserCalendarRepository userCalendarRepository;

	@Autowired
	TokenRepository tokenRepository;

	@Autowired
	PointLogRepository pointLogRepository;

	@Autowired
	FavoriteFieldRepository favoriteFieldRepository;

	@Autowired
	EvaluationRepository evaluationRepository;

	@Autowired
	PointCodeRepository pointCodeRepository;

	@Autowired
	CategoryRepository categoryRepository;

	@Autowired
	GroupMemberRepository groupMemberRepository;

	@Autowired
	GroupInfoRepository groupInfoRepository;

	@Autowired
	CodeRepository codeRepository;

	@Autowired
	KakaoUtil kakaoUtil;

	@Autowired
	UserRepositorySupport userRepositorySupport;

	@Override
	public Optional<User> checkNickname(String nickname) {
		Optional<User> user = userRepository.findByNickname(nickname);

		return user;
	}

	@Override
	public Optional<User> getUserByKakaoId(Long kakaoId) {
		Optional<User> user = userRepository.findByKakaoId(kakaoId);

		return user;
	}

	@Override
	public User insertUser(Long kakaoId) {
		User user = new User();

		user.setKakaoId(kakaoId);

		String userId = "";

		// 새로운 user테이블의 id생성
		while (true) {
			userId = RandomIdUtil.makeRandomId(13);

			if (userRepository.findById(userId).orElse(null) == null)
				break;
		}

		user.setId(userId);

		return userRepository.save(user);
	}

	@Override
	public Token insertToken(OAuthToken oauthToken, Long kakaoId) {
		Optional<User> user = userRepository.findByKakaoId(kakaoId);

		TokenId tokenId = new TokenId();
		tokenId.setTokenId(user.get().getId());

		// 토큰 정보 저장
		Token token = new Token();
		token.setAccessToken(oauthToken.getAccess_token());
		token.setRefreshToken(oauthToken.getRefresh_token());
		token.setUser(user.get());
		token.setTokenId(tokenId);

		// DB에 저장
		return tokenRepository.save(token);
	}

	@Override
	public void deleteToken(String accessToken) {
		long id = kakaoUtil.getKakaoUserIdByToken(accessToken);

		Optional<User> user = userRepository.findByKakaoId(id);

		// userId로 token테이블에서 데이터 삭제
		tokenRepository.deleteByUserId(user.get().getId());
	}

	@Override
	public void singUp(UserRegistPostReq userRegistPostReq, String accessToken) {
		String token = accessToken.split(" ")[1];
		Long kakaoId = kakaoUtil.getKakaoUserIdByToken(token);
		// DB에 저장된 더미 데이터 가져옴
		User user = userRepository.findByKakaoId(kakaoId).orElse(null);

		Code code = new Code();

		// 유저 타입 입력
		code.setCode(userRegistPostReq.getType());

		if (user == null) {
			return;
		}

		// 유저 정보 입력
		user.setNickname(userRegistPostReq.getNickname());
		user.setCode(code);

		UserCalendar userCalendar = new UserCalendar();
		String userCalendarId = "";

		// 새로운 user테이블의 id생성
		while (true) {
			userCalendarId = RandomIdUtil.makeRandomId(13);

			if (userCalendarRepository.findById(userCalendarId).orElse(null) == null)
				break;
		}

		// 유저 시간표 정보
		userCalendar.setId(userCalendarId);
		userCalendar.setPossibleTime(userRegistPostReq.getPossibleTime());
		userCalendar.setUser(user);

		// DB에 저장
		userRepository.save(user);
		userCalendarRepository.save(userCalendar);
	}

	@Override
	public void deleteUser(String accessToken) {
		// accessToken부분
		String token = accessToken.split(" ")[1];
		Long kakaoId = kakaoUtil.getKakaoUserIdByToken(token);

		userRepository.deleteByKakaoId(kakaoId);
	}

	@Override
	public List<SolverRes> getUserList(SolverGetListReq solverGetListReq) {
		// 대분류, 소분류로 조회
		Code mainCategory = codeRepository.findByCode(solverGetListReq.getMainCategory());
		Category subCategory = categoryRepository.findBySubCategoryCode(solverGetListReq.getSubCategory());
		List<User> userList = userRepositorySupport.findAll(mainCategory, subCategory, solverGetListReq.getQuery(), solverGetListReq.getMode());
		
		List<SolverRes> solverList = new ArrayList<SolverRes>();
		for (User user : userList) {
			SolverRes solverRes = new SolverRes();
			
			String profileUrl = user.getProfileUrl();
			String nickname = user.getNickname();
			
			Optional<User> oneUser = userRepository.findByNickname(nickname);
			
			List<PointLog> pointList = oneUser.get().getPointLog();
			List<Evaluation> evaluationList = oneUser.get().getEvaluatedAnswer();
			List<FavoriteField> favoriteFieldList = oneUser.get().getFavoriteField();
			
			/* 포인트 계산 - entity 변경할 예정이어서 수정 필요 */
			int point = 0;
			
			for (PointLog pointLog : pointList) {
				PointCode pointCode = pointCodeRepository.findByPointCode(pointLog.getPointCode().getPointCode());
				
				if(!(pointCode.getPointCode().equals("100") || pointCode.getPointCode().equals("101") || pointCode.getPointCode().equals("102"))) {
					point += pointCode.getValue();
				}
			}
			/* 포인트 계산 끝*/
			
			/* 평점 계산 */			
			float evaluationScore = 0;
			
			for (Evaluation evaluation : evaluationList) {
				evaluationScore += evaluation.getScore();
			}
			
			evaluationScore /= evaluationList.size();			
			/* 평점 계산 끝 */
			
			/* 관심 분야 이름 리스트 생성 */
			List<String> favoriteFieldNameList = new ArrayList<>();
			
			for (FavoriteField favoriteField : favoriteFieldList) {
				favoriteFieldNameList.add(favoriteField.getCategory().getSubCategoryName());
			}
			/* 관심 분야 이름 리스트 생성 끝 */
			
			solverRes.setProfileUrl(profileUrl);
			solverRes.setPoint(point);
			solverRes.setNickname(nickname);
			solverRes.setEvaluationScore(evaluationScore);
			solverRes.setFavoriteFieldNameList(favoriteFieldNameList);
			
			solverList.add(solverRes);
		}
		return solverList;
	}
}
