package com.ie.pdf2.pdfcomp;

import lombok.Data;



//二叉树 节点

@Data
public class TreeMode{
    int data;
    float size;
    TreeMode left=null;
    TreeMode right=null;
    public TreeMode(int item) {
        data = item;
        left = right = null;
    }
}