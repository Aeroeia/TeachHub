package com.teachub.api.client.course;

import com.teachub.api.dto.course.*;
import java.util.List;

public interface CourseClient {

    /**
     * 根据老师id列表获取老师出题数据和讲课数据
     * @param teacherIds 老师id列表
     * @return 老师id和老师对应的出题数和教课数
     */
    List<SubNumAndCourseNumDTO> infoByTeacherIds(Iterable<Long> teacherIds);

    /**
     * 根据小节id获取小节对应的mediaId和课程id
     *
     * @param sectionId 小节id
     * @return 小节对应的mediaId和课程id
     */
    SectionInfoDTO sectionInfo(Long sectionId);

    /**
     * 根据媒资Id列表查询媒资被引用的次数
     *
     * @param mediaIds 媒资id列表
     * @return 媒资id和媒资被引用的次数的列表
     */
    List<MediaQuoteDTO> mediaUserInfo(Iterable<Long> mediaIds);

    /**
     * 根据课程id查询索引库需要的数据
     *
     * @param id 课程id
     * @return 索引库需要的数据
     */
    CourseSearchDTO getSearchInfo(Long id);

    /**
     * 根据课程id集合查询课程简单信息
     * @param ids id集合
     * @return 课程简单信息的列表
     */
    List<CourseSimpleInfoDTO> getSimpleInfoList(Iterable<Long> ids);

    /**
     * 根据课程id，获取课程、目录、教师信息
     * @param id 课程id
     * @return 课程信息、目录信息、教师信息
     */
    CourseFullInfoDTO getCourseInfoById(Long id, boolean withCatalogue, boolean withTeachers);
}
