package com.smartvehicle.service;

import com.smartvehicle.entity.Student;
import com.smartvehicle.entity.SwipeImage;
import com.smartvehicle.entity.SwipeReportMobile;
import com.smartvehicle.payload.request.SwapReportReq;
import com.smartvehicle.payload.response.SwapReportResponse;
import com.smartvehicle.repository.StudentRepository;
import com.smartvehicle.repository.SwipeImageRepository;
import com.smartvehicle.repository.SwipeReportMobileRepository;
import com.smartvehicle.utils.TimestampHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Service
public class SwipeImageService {
    @Autowired
    private SwipeImageRepository swipeImageRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private SwipeReportMobileRepository swipeReportMobileRepository;

    public List<SwipeImage> getSwipesByStudentId(String studentId) {
        return swipeImageRepository.findByStudentId(studentId);
    }

    public List<SwapReportResponse> getSwipesBySchoolAndDateRange(SwapReportReq request) {
        List<SwapReportResponse> swipesReportList = new ArrayList<>();
        List<SwipeImage> swipeImagesList = swipeImageRepository.findBySchoolIdAndDateRange(request.getSchoolId(), request.getStartDate(), request.getEndDate());
        if(swipeImagesList != null && swipeImagesList.size() > 0)
            swipesReportList.addAll(convertFromSwipeImageToSwapReportResp(swipeImagesList));

        Timestamp startDate = new Timestamp(request.getStartDate().getTime());
        startDate = new Timestamp(TimestampHelper.getCalendar(startDate).getTimeInMillis());

        Timestamp endDate = new Timestamp(request.getEndDate().getTime());
        endDate = new Timestamp(TimestampHelper.getCalendar(endDate).getTimeInMillis());
        endDate.setTime(endDate.getTime() + 86399999);

        List<SwipeReportMobile> mobileReports = swipeReportMobileRepository.findBySchoolIdAndDateRange(request.getSchoolId(), startDate, endDate);
        if(mobileReports != null && mobileReports.size() > 0)
            swipesReportList.addAll(convertFromSwipeReportMobileToSwapReportResp(mobileReports));

        return swipesReportList;
    }

    public List<SwapReportResponse> getSwipesBySchoolAndRouteAndDateRange(SwapReportReq request) {
        List<SwapReportResponse> swipesReportList = new ArrayList<>();
        if(request.getRouteId() != null && request.getSchoolId() != null && request.getStudentId() != null) {
            List<SwapReportResponse> responses = getSwipesBySchoolAndRouteAndStudentDateRange(request);
            if (responses.size() > 0)
                swipesReportList.addAll(responses);
        }else if (request.getSchoolId() != null && request.getRouteId() != null) {
            List<SwapReportResponse> responses = getSwipesBySchoolAndRoute(request);
            if (responses.size() > 0)
                swipesReportList.addAll(responses);
        }else if (request.getSchoolId() != null && request.getStudentId() != null) {
            List<SwapReportResponse> responses = getSwipesBySchoolAndStudent(request);
            if (responses.size() > 0)
                swipesReportList.addAll(responses);
        }else if (request.getSchoolId() != null) {
            List<SwapReportResponse> responses = getSwipesBySchool(request);
            if (responses.size() > 0)
                swipesReportList.addAll(responses);
        }
        return swipesReportList;
    }

    public List<SwapReportResponse> getSwipesBySchool(SwapReportReq request) {
        List<SwapReportResponse> swipesReportList = new ArrayList<>();

        List<SwipeImage> swipeImagesList = swipeImageRepository.findBySchoolAndDateRange(request.getSchoolId(), request.getStartDate(), request.getEndDate());
        if(swipeImagesList != null && swipeImagesList.size() > 0)
            swipesReportList.addAll(convertFromSwipeImageToSwapReportResp(swipeImagesList));

        Timestamp startDate = new Timestamp(request.getStartDate().getTime());
        startDate = new Timestamp(TimestampHelper.getCalendar(startDate).getTimeInMillis());

        Timestamp endDate = new Timestamp(request.getEndDate().getTime());
        endDate = new Timestamp(TimestampHelper.getCalendar(endDate).getTimeInMillis());
        endDate.setTime(endDate.getTime() + 86399999);

        List<SwipeReportMobile> mobileReports = swipeReportMobileRepository.findBySchoolAndDateRange(request.getSchoolId(), startDate, endDate);
        if(mobileReports != null && mobileReports.size() > 0)
            swipesReportList.addAll(convertFromSwipeReportMobileToSwapReportResp(mobileReports));

        return swipesReportList;
    }

    public List<SwapReportResponse> getSwipesBySchoolAndStudent(SwapReportReq request) {
        List<SwapReportResponse> swipesReportList = new ArrayList<>();
        List<SwipeImage> swipeImagesList = swipeImageRepository.findBySchoolAndStudentAndDateRange(request.getSchoolId(), request.getStudentId(), request.getStartDate(), request.getEndDate());
        if(swipeImagesList != null && swipeImagesList.size() > 0)
            swipesReportList.addAll(convertFromSwipeImageToSwapReportResp(swipeImagesList));

        Timestamp startDate = new Timestamp(request.getStartDate().getTime());
        startDate = new Timestamp(TimestampHelper.getCalendar(startDate).getTimeInMillis());

        Timestamp endDate = new Timestamp(request.getEndDate().getTime());
        endDate = new Timestamp(TimestampHelper.getCalendar(endDate).getTimeInMillis());
        endDate.setTime(endDate.getTime() + 86399999);

        List<SwipeReportMobile> mobileReports = swipeReportMobileRepository.findBySchoolAndStudentAndDateRange(request.getSchoolId(), request.getStudentId(), startDate, endDate);
        if(mobileReports != null && mobileReports.size() > 0)
            swipesReportList.addAll(convertFromSwipeReportMobileToSwapReportResp(mobileReports));

        return swipesReportList;
    }

    public List<SwapReportResponse> getSwipesBySchoolAndRoute(SwapReportReq request) {
        List<SwapReportResponse> swipesReportList = new ArrayList<>();

        List<SwipeImage> swipeImagesList = swipeImageRepository.findBySchoolAndRouteAndDateRange(request.getSchoolId(), request.getRouteId(), request.getStartDate(), request.getEndDate());
        if(swipeImagesList != null && swipeImagesList.size() > 0)
            swipesReportList.addAll(convertFromSwipeImageToSwapReportResp(swipeImagesList));

        Timestamp startDate = new Timestamp(request.getStartDate().getTime());
        startDate = new Timestamp(TimestampHelper.getCalendar(startDate).getTimeInMillis());

        Timestamp endDate = new Timestamp(request.getEndDate().getTime());
        endDate = new Timestamp(TimestampHelper.getCalendar(endDate).getTimeInMillis());
        endDate.setTime(endDate.getTime() + 86399999);
        List<SwipeReportMobile> mobileReports = swipeReportMobileRepository.findBySchoolAndRouteAndDateRange(request.getSchoolId(), request.getRouteId(), startDate, endDate);
        if(mobileReports != null && mobileReports.size() > 0)
            swipesReportList.addAll(convertFromSwipeReportMobileToSwapReportResp(mobileReports));

        return swipesReportList;
    }

    public List<SwapReportResponse> getSwipesBySchoolAndRouteAndStudentDateRange(SwapReportReq request) {
        List<SwapReportResponse> swipesReportList = new ArrayList<>();
        List<SwipeImage> swipeImagesList = swipeImageRepository.findBySchoolAndRouteAndStudentDateRange(request.getSchoolId(), request.getRouteId(), request.getStudentId(), request.getStartDate(), request.getEndDate());

        if(swipeImagesList != null && swipeImagesList.size() > 0)
            swipesReportList.addAll(convertFromSwipeImageToSwapReportResp(swipeImagesList));

        Timestamp startDate = new Timestamp(request.getStartDate().getTime());
        startDate = new Timestamp(TimestampHelper.getCalendar(startDate).getTimeInMillis());

        Timestamp endDate = new Timestamp(request.getEndDate().getTime());
        endDate = new Timestamp(TimestampHelper.getCalendar(endDate).getTimeInMillis());
        endDate.setTime(endDate.getTime() + 86399999);

        List<SwipeReportMobile> mobileReports = swipeReportMobileRepository.findBySchoolAndRouteAndStudentDateRange(request.getSchoolId(), request.getRouteId(), request.getStudentId(), startDate, endDate);
        if(mobileReports != null && mobileReports.size() > 0)
            swipesReportList.addAll(convertFromSwipeReportMobileToSwapReportResp(mobileReports));

        return swipesReportList;
    }

    public List<SwapReportResponse> convertFromSwipeReportMobileToSwapReportResp(List<SwipeReportMobile> mobileReports){
        List<SwapReportResponse> swipesReportList = new ArrayList<>();
        mobileReports.stream().forEach(mobileReport -> {
            SwapReportResponse response = new SwapReportResponse();
            response.setStudentId(mobileReport.getStudentId());
            Student Std = studentRepository.findByRouteSchoolAndStudentId(response.getStudentId());
            response.setSchoolId(mobileReport.getSchoolId());
            response.setStudentName(Std.getFirstName() + " " + Std.getLastName());
            response.setRouteId(mobileReport.getRouteId());
            response.setLatitude(mobileReport.getLatitude());
            response.setLongitude(mobileReport.getLongitude());
            response.setImageName(mobileReport.getImageName());
            swipesReportList.add(response);
        });
        return swipesReportList;
    }

    public List<SwapReportResponse> convertFromSwipeImageToSwapReportResp(List<SwipeImage> swipeImagesList){
        List<SwapReportResponse> swipesReportList = new ArrayList<>();
        swipeImagesList.stream().forEach(swipeImage -> {
            SwapReportResponse response = new SwapReportResponse();
            response.setStudentId(swipeImage.getStudentId());
            Student Std = studentRepository.findByRouteSchoolAndStudentId(response.getStudentId());
            response.setSchoolId(swipeImage.getSchId());
            response.setStudentName(Std.getFirstName() + " " + Std.getLastName());
            response.setRouteId(swipeImage.getRouteId());
            response.setLatitude(swipeImage.getLatitude());
            response.setLongitude(swipeImage.getLongitude());
            response.setImageName(swipeImage.getImageName());
            swipesReportList.add(response);
        });

        return swipesReportList;
    }

}

