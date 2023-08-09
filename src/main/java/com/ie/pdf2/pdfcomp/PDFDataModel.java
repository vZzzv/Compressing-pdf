package com.ie.pdf2.pdfcomp;


import lombok.Data;

import java.util.HashMap;
import java.util.Map;



@Data
public class PDFDataModel {
   Map<String,PSModel> pdfMap = new HashMap<>();
}
