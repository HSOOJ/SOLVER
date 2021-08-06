package com.solver.api.service;

import java.util.List;
import java.util.Optional;

import com.solver.api.request.SolverGetListReq;
import com.solver.api.request.UserRegistPostReq;
import com.solver.api.response.SolverRes;
import com.solver.common.model.OAuthToken;
import com.solver.db.entity.user.Token;
import com.solver.db.entity.user.User;

public interface UserService {
	Optional<User> checkNickname(String nickname);

	Optional<User> getUserByKakaoId(Long kakaoId);

	User insertUser(Long kakaoId);

	Token insertToken(OAuthToken oauthToken, Long kakaoId);

	void deleteToken(String accessToken);

	void singUp(UserRegistPostReq userRegistPostReq, String accessToken);

	void deleteUser(String accessToken);

	List<SolverRes> getUserList(SolverGetListReq solverGetListReq);

	String getNickname(String accessToken);
}