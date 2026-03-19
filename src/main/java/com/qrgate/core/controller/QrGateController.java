package com.qrgate.core.controller;

import com.qrgate.core.service.QrCodeService;
import com.qrgate.core.service.QrGateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth/qr")
@Slf4j
@RequiredArgsConstructor
public class QrGateController {

    private final QrGateService qrGateService;
    private final QrCodeService qrCodeService;
}
