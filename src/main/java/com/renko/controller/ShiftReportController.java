package com.renko.controller;

import com.renko.exceptions.UserException;
import com.renko.payload.dto.ShiftReportDto;
import com.renko.service.ShiftReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shift-report")
public class ShiftReportController
{
    private final ShiftReportService shiftReportService;

    @PostMapping("/start")
    public ResponseEntity<ShiftReportDto> startShift() throws Exception
    {
        return ResponseEntity.ok(shiftReportService.startShift());
    }

    @PatchMapping("/end")
    public ResponseEntity<ShiftReportDto> endShift() throws Exception
    {
        return  ResponseEntity.ok(shiftReportService.endShift(null, LocalDateTime.now()));
    }

    @GetMapping("/current")
    public ResponseEntity<ShiftReportDto> getCurrentShiftProgress() throws UserException
    {
        return ResponseEntity.ok(shiftReportService.getCurrentShiftProgress(null));
    }

    @GetMapping("/cashier/{cashierId}/by-date")
    public ResponseEntity<ShiftReportDto> getShiftReportByDate(@PathVariable Long cashierId,
                                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) throws Exception
    {
        return ResponseEntity.ok(shiftReportService.getShiftByCashierEntityAndDate(cashierId, date));
    }

    @GetMapping("/cashier/{cashierId}")
    public ResponseEntity<List<ShiftReportDto>> getShiftReportByCashier(@PathVariable Long cashierId)
    {
        return ResponseEntity.ok(shiftReportService.getShiftReportsByCashierEntity_Id(cashierId));
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<ShiftReportDto>> getShiftReportByStore(@PathVariable Long storeId)
    {
        return ResponseEntity.ok(shiftReportService.getShiftReportsByStoreId(storeId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShiftReportDto> getShiftReportById(@PathVariable Long id) throws Exception
    {
        return ResponseEntity.ok(shiftReportService.getShiftReportById(id));
    }
}
