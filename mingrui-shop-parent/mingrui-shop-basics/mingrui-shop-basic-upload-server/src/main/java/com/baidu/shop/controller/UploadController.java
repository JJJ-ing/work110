package com.baidu.shop.controller;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.status.HTTPStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping(value = "upload")
public class UploadController extends BaseApiService {
    //linux系统的上传目录
    @Value(value = "${mingrui.upload.path.windows}")
    private String windowsPath;

    //window系统的上传目录
    @Value(value = "${mingrui.upload.path.linux}")
    private String linuxPath;

    @Value(value = "${mingrui.upload.img.host}")
    private String imageHost;

    @PostMapping
    public Result<String> uploadImg(@RequestParam MultipartFile file) {
        if (file.isEmpty()) return this.setResultError("上传的文件为空");
        String filename = file.getOriginalFilename();

        String path = "";
        String os = System.getProperty("os.name").toLowerCase();
        //String path = os.indexOf("win") != -1 ? windowsPath : os.indexOf("lin") != -1 ? linuxPath : "";
        if(os.indexOf("win") != -1){
            path = windowsPath;
        }else if(os.indexOf("lin") != -1){
            path = linuxPath;
        }

        filename = UUID.randomUUID() + filename;

        File dest = new File(path + File.separator + filename);

        if(!dest.getParentFile().exists()) dest.getParentFile().mkdirs();

        try {
            file.transferTo(dest);//上传
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return this.setResult(HTTPStatus.OK,"upload success!!!",imageHost + "/" + filename);
    }
}
