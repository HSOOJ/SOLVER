package com.solver.api.service;

import java.util.List;

import com.solver.api.request.AnswerCreatePostReq;
import com.solver.api.request.AnswerUpdatePatchReq;
import com.solver.db.entity.answer.Answer;

public interface AnswerService {

	void createAnswer(String accessToken, AnswerCreatePostReq answerCreateGetReq, String questionId);

	boolean deleteAnswer(String accessToken, String answerId);

	boolean updateAnswer(String accessToken, String answerId, AnswerUpdatePatchReq answerUpdatePatchReq);

	List<Answer> getAnswerList(String questionId);
	
}