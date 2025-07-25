package com.teachub.data.service.impl;

import com.teachub.common.utils.BeanUtils;
import com.teachub.common.utils.JsonUtils;
import com.teachub.data.constants.RedisConstants;
import com.teachub.data.model.dto.Top10DataSetDTO;
import com.teachub.data.model.po.CourseInfo;
import com.teachub.data.model.vo.Top10DataVO;
import com.teachub.data.service.Top10Service;
import com.teachub.data.utils.DataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName Top10ServiceImpl
 *  wusongsong
 * @Date /10/10 19:46
 * @Version
 **/
@Service
public class Top10ServiceImpl implements Top10Service {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Top10DataVO getTop10Data() {
        // 1.数据redis存储key
        String key = RedisConstants.KEY_TOP10 + DataUtils.getVersion(1);
        // 2.获取数据
        Object originData = redisTemplate.opsForValue().get(key);
        // 2.1.数据判空
        if (originData == null) {
            return new Top10DataVO();
        }
        // 3.数据转换成课程信息
        List<CourseInfo> data = JsonUtils.toList(originData.toString(), CourseInfo.class);
        // 4.数据组装
        Top10DataVO top10DataVO = new Top10DataVO();
        // 4.1.设置热门课程
        top10DataVO.setHot(data.stream()
                .sorted(Comparator.comparing(CourseInfo::getNewStuNum).reversed())
                .limit(10)
                .collect(Collectors.toList()));
        // 4.2.设置热销课程
        top10DataVO.setHotSales(data.stream()
                .sorted(Comparator.comparing(CourseInfo::getOrderAmount).reversed())
                .limit(10)
                .collect(Collectors.toList()));
        return top10DataVO;
    }

    @Override
    public void setTop10Data(Top10DataSetDTO top10DataSetDTO) {
        // 1.数据redis存储key
        String key = RedisConstants.KEY_TOP10 + top10DataSetDTO.getVersion();
        // 2.数据转化
        List<CourseInfo> courseInfoList = BeanUtils.copyList(top10DataSetDTO.getData(), CourseInfo.class);

        //3.新增或重置数据
        redisTemplate.opsForValue().set(
                key,
                JsonUtils.toJsonStr(courseInfoList)
        );
    }
}
