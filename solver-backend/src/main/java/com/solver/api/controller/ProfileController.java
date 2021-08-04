package com.solver.api.controller;

import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.solver.api.request.ProfilePossibleTimePatchReq;
import com.solver.api.request.ProfileUpdatePatchReq;
import com.solver.api.response.ProfileRes;
import com.solver.api.response.ProfileTabRes;
import com.solver.api.service.ProfileService;
import com.solver.common.model.BaseResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@CrossOrigin(origins = "http://localhost:8081", allowCredentials="true", allowedHeaders="*",
methods= {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT, RequestMethod.HEAD, RequestMethod.OPTIONS})

@Api(value="유저 API", tags = {"Profile"})
@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {
	
	@Autowired
	ProfileService	profileService;
	
	/* 유저 프로필 정보 조회 */
	@GetMapping("/{nickname}")
	@ApiOperation(value = "회원 프로필 정보", notes = "회원 마이페이지 기본 정보 조회") 
    @ApiResponses({
        @ApiResponse(code = 200, message = "정보를 가져오는데 성공하였습니다"),
        @ApiResponse(code = 409, message = "정보를 가져오는데 실패하였습니다")
    })
	public ResponseEntity<? extends BaseResponse> getProfileInfo(@PathVariable String nickname)
	{
		ProfileRes profileRes = null;
		
		try {
			profileRes = profileService.getProfileInfo(nickname);
		}
		catch(NoSuchElementException e) {
			return ResponseEntity.status(409).body(ProfileRes.of(409, "정보를 가져오는데 실패하였습니다"));
		}
		
		
		return ResponseEntity.status(200).body(ProfileRes.of(200, "정보를 가져오는데 성공하였습니다", profileRes));
	}
	
	/* 유저 프로필 정보 수정 */
	@PatchMapping()
	@ApiOperation(value = "회원 프로필 정보 수정(시간제외)", notes = "회원 마이페이지 기본 정보 수정") 
    @ApiResponses({
        @ApiResponse(code = 201, message = "프로필 정보 수정에 성공하였습니다"),
        @ApiResponse(code = 409, message = "프로필 정보 수정에 실패하였습니다")
    })
	public ResponseEntity<? extends BaseResponse> updateProfile(
			@ApiIgnore @RequestHeader("Authorization") String accessToken,
			@RequestBody ProfileUpdatePatchReq profileUpdatePatchReq)
	{
		profileService.updateProfile(profileUpdatePatchReq, accessToken);
		
		return ResponseEntity.status(201).body(BaseResponse.of(201, "프로필 정보 수정에 성공하였습니다"));
	}
	
	/* 유저 화상 회의 테이블 정보 수정 */
	@PatchMapping("/time-table")
	@ApiOperation(value = "회원 화상 회의 테이블 정보 수정", notes = "회원 화상 회의 테이블 정보 수정") 
    @ApiResponses({
        @ApiResponse(code = 201, message = "화상 회의 테이블 정보 수정에 성공하였습니다"),
        @ApiResponse(code = 409, message = "화상 회의 테이블 정보 수정에 실패하였습니다")
    })
	public ResponseEntity<? extends BaseResponse> updateProfilePossibleTime(
			@ApiIgnore @RequestHeader("Authorization") String accessToken,
			@RequestBody ProfilePossibleTimePatchReq profilePossibleTimePatchReq)
	{
		profileService.updateProfilePossibleTime(profilePossibleTimePatchReq, accessToken);
		
		return ResponseEntity.status(201).body(BaseResponse.of(201, "화상 회의 테이블 정보 수정에 성공하였습니다"));
	}
	
	/* 유저 탭 정보 조회
	 * tabNum
	 * 0: SOLVE 기록
	 * 1: 답변 목록
	 * 2: 질문 목록 
	 * */
	@PatchMapping("/{nickname}/tab")
	@ApiOperation(value = "회원 화상 회의 테이블 정보 수정", notes = "회원 화상 회의 테이블 정보 수정") 
    @ApiResponses({
        @ApiResponse(code = 201, message = "성공하였습니다"),
        @ApiResponse(code = 409, message = "실패하였습니다")
    })
	public ResponseEntity<? extends BaseResponse> profileTabInfo(
			@PathVariable String nickname,
			@RequestParam int tabNum)
	{
		ProfileTabRes profileTabRes = profileService.getProfileTabInfo(nickname, tabNum);
		profileTabRes.setMessage("성공하였습니다");
		profileTabRes.setStatusCode(201);
		
		return ResponseEntity.status(201).body(profileTabRes);
	}
}
