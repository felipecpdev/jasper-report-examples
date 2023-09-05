package com.felipecpdev.certificatewithjasperreport.controllers;

import net.sf.jasperreports.engine.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/report")
public class CertificateController {

    private static final String CLASSPATH = "classpath:";
    private static final String TEMPLATE_REPORTS = "templates/reports/";


    @Autowired
    private ResourceLoader resourceLoader;

    @GetMapping(value = "/download-certificate", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<Resource> downloadReport() throws JRException, IOException {
        try {
            Map<String, Object> parameters = new HashMap<>();
            File file = ResourceUtils.getFile(CLASSPATH + TEMPLATE_REPORTS + "certificate-udemy-example.jrxml");
            Resource imageResource = resourceLoader.getResource(CLASSPATH + "static/img/" + "udemy-logo.png");

            JasperReport compileReport = JasperCompileManager.compileReport(file.getAbsolutePath());

            String nameStudent = "Felipe Contreras";
            String logoUdemy = imageResource.getURL().getPath();

            parameters.put("NAME_STUDENT", nameStudent);
            parameters.put("LOGO_UDEMY", logoUdemy);

            // Generar el informe
            JasperPrint jasperPrint = JasperFillManager.fillReport(compileReport, parameters, new JREmptyDataSource());
            // Exportar el informe a un formato compatible (por ejemplo, PDF)
            byte[] data = JasperExportManager.exportReportToPdf(jasperPrint);
            // Crear un recurso ByteArrayResource
            ByteArrayResource resource = new ByteArrayResource(data);
            // Configurar las cabeceras de la respuesta
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=certificate.pdf");
            // Crea el ResponseEntity y devolverlo
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .badRequest()
                    .body(new ByteArrayResource(new byte[0]));
        }
    }
}
