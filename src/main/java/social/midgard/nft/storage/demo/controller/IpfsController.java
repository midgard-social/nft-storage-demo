package social.midgard.nft.storage.demo.controller;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author midgard
 */
@CrossOrigin(origins = "*")
@Slf4j
@Controller
public class IpfsController {

    @Value("${nft.url:https://api.nft.storage/upload}")
    private String nftUrl;

    @Value("${nft.token:}")
    private String token;

    @Value("${nft.ipfs-gateway:https://ipfs-gateway.cloud/ipfs/}")
    private String gateway;


    @ResponseBody
    @PostMapping(value = "/nft/pic")
    public String uploadNftPic(@RequestParam("pic") MultipartFile pic) throws IOException {

        try (var httpRsp = HttpRequest
                .post(nftUrl)
                .header(Header.AUTHORIZATION, "Bearer " + token)
                .body(pic.getBytes())
                .execute()
        ) {
            JSONObject responseObject = JSON.parseObject(httpRsp.body());
            boolean success = responseObject.getBoolean("ok");
            if (!success) {
                log.info("Failed to upload to nft, result is：{} ", httpRsp.body());
                return "";
            }
            return responseObject.getJSONObject("value").getString("cid");
        }
    }

    @GetMapping(path = "/nft/pic")
    public void download(@RequestParam String cid) throws IOException {

        try (var httpRsp = HttpRequest
                .get(gateway + cid)
                .execute()
        ) {
            byte[] fileBytes = httpRsp.bodyBytes();

            val response = ((ServletRequestAttributes)
                    (RequestContextHolder.currentRequestAttributes())).getResponse();

            assert response != null;
            response.setContentType("image/jpeg");
            response.addHeader("Content-Length", "" + fileBytes.length);
            response.setHeader("Content-Disposition",
                    "inline; filename=\"" + URLUtil.encode(cid) + "\"");

            IoUtil.write(response.getOutputStream(), true, fileBytes);
        }
    }

}
