package com.study.checkin.controller;

import com.study.checkin.common.Result;
import com.study.checkin.dto.CheckInRequest;
import com.study.checkin.entity.CheckIn;
import com.study.checkin.service.CheckInService;
import com.study.checkin.vo.StatsVO;
import com.study.checkin.vo.TodayVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    @Autowired
    private CheckInService checkInService;

    @PostMapping
    public Result<?> checkIn(@Valid @RequestBody CheckInRequest req, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        checkInService.checkIn(userId, req);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        checkInService.delete(userId, id);
        return Result.ok();
    }

    @GetMapping("/{id}")
    public Result<CheckIn> get(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(checkInService.get(userId, id));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody CheckInRequest req, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        checkInService.update(userId, id, req);
        return Result.ok();
    }

    @GetMapping("/today")
    public Result<TodayVO> today(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(checkInService.today(userId));
    }

    @GetMapping("/list")
    public Result<List<CheckIn>> list(@RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "20") int size,
                                      HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(checkInService.list(userId, page, size));
    }

    @GetMapping("/stats")
    public Result<StatsVO> stats(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(checkInService.stats(userId));
    }
}
