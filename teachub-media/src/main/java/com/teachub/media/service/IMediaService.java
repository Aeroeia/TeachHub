package com.teachub.media.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teachub.common.domain.dto.PageDTO;
import com.teachub.media.domain.dto.MediaDTO;
import com.teachub.media.domain.dto.MediaUploadResultDTO;
import com.teachub.media.domain.po.Media;
import com.teachub.media.domain.query.MediaQuery;
import com.teachub.media.domain.vo.MediaVO;
import com.teachub.media.domain.vo.VideoPlayVO;

/**
 * <p>
 * 媒资表，主要是视频文件 服务类
 * </p>
 *
 *
 * @  06-30
 */
public interface IMediaService extends IService<Media> {

    String getUploadSignature();

    VideoPlayVO getPlaySignatureBySectionId(Long fileId);

    MediaDTO save(MediaUploadResultDTO mediaResult);

    void updateMediaProcedureResult(Media media);

    void deleteMedia(String fileId);

    VideoPlayVO getPlaySignatureByMediaId(Long mediaId);

    PageDTO<MediaVO> queryMediaPage(MediaQuery query);
}
