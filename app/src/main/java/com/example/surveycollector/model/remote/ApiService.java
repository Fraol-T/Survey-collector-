package com.example.surveycollector.model.remote;

import java.util.Map;
import com.example.surveycollector.model.remote.dto.AnswerDto;
import com.example.surveycollector.model.remote.dto.FirebaseKey;
import com.example.surveycollector.model.remote.dto.QuestionDto;
import com.example.surveycollector.model.remote.dto.QuestionOptionDto;
import com.example.surveycollector.model.remote.dto.RespondentDto;
import com.example.surveycollector.model.remote.dto.ResponseDto;
import com.example.surveycollector.model.remote.dto.SurveyDto;
import com.example.surveycollector.model.remote.dto.SurveyLinkDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    @POST("questions.json")
    Call<FirebaseKey> postQuestion(@Body QuestionDto question);

    @DELETE("questions/{key}.json")
    Call<Void> deleteQuestion(@Path("key") String key);

    @POST("questionoptions.json")
    Call<FirebaseKey> postQuestionOption(@Body QuestionOptionDto option);

    @DELETE("questionoptions/{key}.json")
    Call<Void> deleteQuestionOption(@Path("key") String key);

    @GET("surveys.json")
    Call<Map<String, SurveyDto>> getSurveys();

    @POST("surveys.json")
    Call<FirebaseKey> postSurvey(@Body SurveyDto survey);

    @PUT("surveys/{key}.json")
    Call<SurveyDto> putSurvey(@Path("key") String key, @Body SurveyDto survey);

    @DELETE("surveys/{key}.json")
    Call<Void> deleteSurvey(@Path("key") String key);

    @GET("responses.json")
    Call<Map<String, ResponseDto>> getResponses();

    @POST("responses.json")
    Call<FirebaseKey> postResponse(@Body ResponseDto response);

    @GET("respondents.json")
    Call<Map<String, RespondentDto>> getRespondents();

    @POST("respondents.json")
    Call<FirebaseKey> postRespondent(@Body RespondentDto respondent);

    @POST("answers.json")
    Call<FirebaseKey> postAnswer(@Body AnswerDto answer);

    @GET("answers.json")
    Call<Map<String, AnswerDto>> getAnswers();

    @GET("surveylinks.json")
    Call<Map<String, SurveyLinkDto>> getSurveyLinks();

    @POST("surveylinks.json")
    Call<FirebaseKey> postSurveyLink(@Body SurveyLinkDto surveyLink);

    @PATCH("surveylinks/{key}.json")
    Call<SurveyLinkDto> patchSurveyLink(@Path("key") String key, @Body SurveyLinkDto surveyLink);
}
