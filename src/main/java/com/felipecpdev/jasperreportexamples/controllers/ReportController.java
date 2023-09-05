package com.felipecpdev.jasperreportexamples.controllers;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
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
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping(value = "/api/v1/report")
public class ReportController {

    private static final String CLASSPATH = "classpath:";
    private static final String TEMPLATE_REPORTS = "templates/reports/";

    private final ResourceLoader resourceLoader;

    public ReportController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @GetMapping(value = "/download-certificate", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<Resource> downloadReport() throws JRException, IOException {
        try {
            Map<String, Object> parameters = new HashMap<>();
            File file = ResourceUtils.getFile(CLASSPATH + TEMPLATE_REPORTS + "certificate-udemy-example.jrxml");
            Resource imageResource = resourceLoader.getResource(CLASSPATH + "static/img/" + "udemy-logo.png");
            Date now = new Date();
            //compila el archivo
            JasperReport compileReport = JasperCompileManager.compileReport(file.getAbsolutePath());


            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");
            String formattedDate = sdf.format(now);

            String nameStudent = "Felipe Contreras";
            String logoUdemy = imageResource.getURL().getPath();
            String courseTitle = "Master Spring Data JPA with Hibernate: E-Commerce Project";
            String uuid = UUID.randomUUID().toString();
            String duration = "20 horas";
            String urlCert = "ude.my/".concat(uuid);
            String ref = "004";

            //ejemplo de una lista simple con JRBeanCollectionDataSource
            List<String> instructorsNames = List.of("Ramesh Fadatare", "Pablo Contreras");
            JRBeanCollectionDataSource instructorsCollection =
                    new JRBeanCollectionDataSource(instructorsNames);

            //String filename = "CU_".concat(nameStudent).concat("_").concat(formattedDate);
            String filename = "CU-".concat(uuid);
            //parametros establecidos en el reporte
            parameters.put("NAME_STUDENT", nameStudent);
            parameters.put("LOGO_UDEMY", logoUdemy);
            parameters.put("COURSE_TITLE", courseTitle);
            parameters.put("UUID", uuid);
            parameters.put("COURSE_DATE", now);
            parameters.put("DURATION", duration);
            parameters.put("URL", urlCert);
            parameters.put("REF", ref);
            parameters.put("INSTRUCTORS", instructorsCollection);

            // Generar el informe
            JasperPrint jasperPrint = JasperFillManager.fillReport(compileReport, parameters, new JREmptyDataSource());
            // Exportar el informe a un formato compatible (por ejemplo, PDF)
            byte[] data = JasperExportManager.exportReportToPdf(jasperPrint);
            // Crear un recurso ByteArrayResource
            ByteArrayResource resource = new ByteArrayResource(data);
            // Configurar las cabeceras de la respuesta
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=".concat(filename).concat(".pdf"));
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
