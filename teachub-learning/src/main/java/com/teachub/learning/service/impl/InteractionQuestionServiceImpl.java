package com.teachub.learning.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teachub.api.cache.CategoryCache;
import com.teachub.api.client.course.CatalogueClient;
import com.teachub.api.client.course.CourseClient;
import com.teachub.api.client.search.SearchClient;
import com.teachub.api.client.user.UserClient;
import com.teachub.api.dto.course.CataSimpleInfoDTO;
import com.teachub.api.dto.course.CourseSimpleInfoDTO;
import com.teachub.api.dto.user.UserDTO;
import com.teachub.common.domain.dto.PageDTO;
import com.teachub.common.exceptions.BadRequestException;
import com.teachub.common.exceptions.BizIllegalException;
import com.teachub.common.utils.BeanUtils;
import com.teachub.common.utils.CollUtils;
import com.teachub.common.utils.UserContext;
import com.teachub.learning.domain.dto.QuestionAdminPageQuery;
import com.teachub.learning.domain.dto.QuestionFormDTO;
import com.teachub.learning.domain.dto.QuestionPageQuery;
import com.teachub.learning.domain.po.InteractionQuestion;
import com.teachub.learning.domain.po.InteractionReply;
import com.teachub.learning.domain.vo.QuestionAdminVO;
import com.teachub.learning.domain.vo.QuestionVO;
import com.teachub.learning.mapper.InteractionQuestionMapper;
import com.teachub.learning.service.IInteractionQuestionService;
import com.teachub.learning.service.IInteractionReplyService;
import io.swagger.v3.oas.models.links.Link;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>
 * 互动提问的问题表 服务实现类
 * </p>
 *
 * @author Aeroeia
 * @since 2025-07-19
 */
@Service
@RequiredArgsConstructor
public class InteractionQuestionServiceImpl extends ServiceImpl<InteractionQuestionMapper, InteractionQuestion> implements IInteractionQuestionService {
    private final UserClient userClient;
    private final IInteractionReplyService replyService;
    private final SearchClient searchClient;
    private final CourseClient courseClient;
    private final CatalogueClient catalogueClient;
    private final CategoryCache categoryCache;
    @Override
    public void addQuestion(QuestionFormDTO questionFormDTO) {
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BadRequestException("用户未登录");
        }
        InteractionQuestion interactionQuestion = BeanUtils.copyBean(questionFormDTO, InteractionQuestion.class);
        interactionQuestion.setUserId(userId);
        this.save(interactionQuestion);
    }

    @Override
    public void updateQuestion(QuestionFormDTO questionFormDTO, Long id) {
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BadRequestException("用户未登录");
        }
        InteractionQuestion interactionQuestion = this.lambdaQuery().eq(InteractionQuestion::getId, id).one();
        if (interactionQuestion == null) {
            throw new BadRequestException("问题不存在");
        }
        if (!interactionQuestion.getUserId().equals(userId)) {
            throw new BizIllegalException("不能修改他人问题");
        }
        String title = questionFormDTO.getTitle();
        String description = questionFormDTO.getDescription();
        Boolean anonymity = questionFormDTO.getAnonymity();
        this.lambdaUpdate().eq(InteractionQuestion::getId, id)
                .set(StrUtil.isNotBlank(title), InteractionQuestion::getTitle, title)
                .set(StrUtil.isNotBlank(description), InteractionQuestion::getDescription, description)
                .set(anonymity != null, InteractionQuestion::getAnonymity, anonymity)
                .update();
    }

    @Override
    public PageDTO<QuestionVO> queryQuestions(QuestionPageQuery pageQuery) {
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BadRequestException("用户未登录");
        }
        //构造分页参数
        Page<InteractionQuestion> page = pageQuery.toMpPageDefaultSortByCreateTimeDesc();
        Page<InteractionQuestion> p = this.lambdaQuery().eq(pageQuery.getOnlyMine() != null && pageQuery.getOnlyMine(), InteractionQuestion::getUserId, userId)
                .eq(InteractionQuestion::getCourseId, pageQuery.getCourseId())
                .eq(pageQuery.getSectionId() != null, InteractionQuestion::getSectionId, pageQuery.getSectionId())
                .eq(InteractionQuestion::getHidden, false)
                //排除description字段
                .select(InteractionQuestion.class, new Predicate<TableFieldInfo>() {
                    @Override
                    public boolean test(TableFieldInfo tableFieldInfo) {
                        return !tableFieldInfo.getProperty().equals("description");
                    }
                })
                .page(page);
        List<InteractionQuestion> records = p.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(p);
        }
        //根据最新回答用户id进行分类
        List<Long> answerIds = records.stream().map(InteractionQuestion::getLatestAnswerId).filter(Objects::nonNull).collect(Collectors.toList());
        List<InteractionReply> interactionReplies = new ArrayList<>();
        if(CollUtils.isNotEmpty(answerIds)){
            interactionReplies = replyService.listByIds(answerIds);
        }
        Set<Long> collect = interactionReplies.stream().map(InteractionReply::getUserId).collect(Collectors.toSet());
        List<Long> userIds = records.stream().map(InteractionQuestion::getUserId).collect(Collectors.toList());
        collect.addAll(userIds);
        List<UserDTO> users = userClient.queryUserByIds(collect);
        Map<Long, UserDTO> userMap = users.stream().collect(Collectors.toMap(UserDTO::getId, u -> u));
        Map<Long, InteractionReply> interactionReplyMap = interactionReplies.stream().collect(Collectors.toMap(InteractionReply::getId, r -> r));
        List<QuestionVO> result = new ArrayList<>();
        //po->vo
        for (InteractionQuestion question : records) {
            QuestionVO questionVO = BeanUtils.toBean(question, QuestionVO.class);
            if (!questionVO.getAnonymity()) {
                questionVO.setUserName(userMap.get(question.getUserId()).getName());
                questionVO.setUserIcon(userMap.get(question.getUserId()).getIcon());
            }
            if (question.getLatestAnswerId() != null) {
                questionVO.setLatestReplyContent(interactionReplyMap.get(question.getLatestAnswerId()).getContent());
                questionVO.setLatestReplyUser(userMap.get(interactionReplyMap.get(question.getLatestAnswerId()).getUserId()).getName());
            }

            result.add(questionVO);
        }
        return PageDTO.of(p, result);
    }

    @Override
    public QuestionVO queryById(Long id) {
        //非法校验
        if (id == null) {
            throw new BadRequestException("问题id不能为空");
        }
        InteractionQuestion interactionQuestion = this.lambdaQuery().eq(InteractionQuestion::getId, id).one();
        if (interactionQuestion == null) {
            throw new BadRequestException("问题不存在");
        }
        //判断是否隐藏
        if (interactionQuestion.getHidden()) {
            return null;
        }
        QuestionVO questionVO = BeanUtils.copyBean(interactionQuestion, QuestionVO.class);
        if (!interactionQuestion.getAnonymity()) {
            UserDTO userDTO = userClient.queryUserById(interactionQuestion.getUserId());
            if (userDTO == null) {
                throw new BadRequestException("用户不存在");
            }
            questionVO.setUserName(userDTO.getName());
            questionVO.setUserIcon(userDTO.getIcon());
        }
        return questionVO;
    }

    @Override
    public void delete(Long id) {
        Long userId = UserContext.getUser();
        if (userId == null) {
            throw new BadRequestException("用户未登录");
        }
        InteractionQuestion interactionQuestion = this.lambdaQuery().eq(InteractionQuestion::getId, id).one();
        if (interactionQuestion == null) {
            throw new BadRequestException("问题不存在");
        }
        if (!userId.equals(interactionQuestion.getUserId())) {
            throw new BizIllegalException("不能删除他人问题");
        }
        boolean remove = this.lambdaUpdate().eq(InteractionQuestion::getId, id).remove();
        if (!remove) {
            throw new BizIllegalException("删除问题失败");
        }
    }

    @Override
    public PageDTO<QuestionAdminVO> queryAdminQuestions(QuestionAdminPageQuery query) {
        //通过es根据课程名查询相关课程
        String courseName = query.getCourseName();
        List<Long> ids = new ArrayList<>();
        if(StrUtil.isNotBlank(courseName)){
            ids = searchClient.queryCoursesIdByName(courseName);
            if(CollUtils.isEmpty(ids)){
                return PageDTO.empty(0L,0L);
            }
        }
        //构造查询条件
        Page<InteractionQuestion> page = this.lambdaQuery().in(CollUtils.isNotEmpty(ids), InteractionQuestion::getCourseId, ids)
                .eq(query.getStatus() != null, InteractionQuestion::getStatus, query.getStatus())
                .between(query.getBeginTime() != null && query.getEndTime() != null,
                        InteractionQuestion::getUpdateTime, query.getBeginTime(), query.getEndTime())
                .page(query.toMpPageDefaultSortByCreateTimeDesc());
        List<InteractionQuestion> records = page.getRecords();
        if(CollUtils.isEmpty(records)){
            return PageDTO.empty(page);
        }
        //查询用户名
        Set<Long> userIds = records.stream().map(InteractionQuestion::getUserId).collect(Collectors.toSet());
        List<UserDTO> userDTOS = userClient.queryUserByIds(userIds);
        Map<Long, String> userMap = userDTOS.stream().collect(Collectors.toMap(UserDTO::getId, UserDTO::getName));

        //查询课程名
        List<CourseSimpleInfoDTO> courseInfos = courseClient.getSimpleInfoList(ids);
        Map<Long, CourseSimpleInfoDTO> courseMap = courseInfos.stream().collect(Collectors.toMap(CourseSimpleInfoDTO::getId, u -> u));

        //查询章/节名
        Set<Long> catalogueIds = new HashSet<>();
        for(InteractionQuestion question : records){
            Long chapterId = question.getChapterId();
            Long sectionId = question.getSectionId();
            if(chapterId!=null){
                catalogueIds.add(chapterId);
            }
            if(sectionId!=null){
                catalogueIds.add(sectionId);
            }
        }
        List<CataSimpleInfoDTO> cataSimpleInfoDTOS = catalogueClient.batchQueryCatalogue(catalogueIds);
        Map<Long, String> cataMap = cataSimpleInfoDTOS.stream().collect(Collectors.toMap(CataSimpleInfoDTO::getId, CataSimpleInfoDTO::getName));

        List<QuestionAdminVO> result = new ArrayList<>();
        //po->vo
        for(InteractionQuestion question : records){
            QuestionAdminVO questionAdminVO = BeanUtils.copyBean(question, QuestionAdminVO.class);
            //用户名
            questionAdminVO.setUserName(userMap.get(question.getUserId()));
            //课程
            CourseSimpleInfoDTO courseSimpleInfoDTO = courseMap.get(question.getCourseId());
            questionAdminVO.setCourseName(courseSimpleInfoDTO.getName());
            List<Long> categoryIds = courseSimpleInfoDTO.getCategoryIds();
            String categoryNames = categoryCache.getCategoryNames(categoryIds);
            questionAdminVO.setCategoryName(categoryNames);
            //章节名
            questionAdminVO.setChapterName(cataMap.get(question.getChapterId())==null?"":cataMap.get(question.getChapterId()));
            questionAdminVO.setSectionName(cataMap.get(question.getSectionId())==null?"":cataMap.get(question.getSectionId()));
            result.add(questionAdminVO);
        }
        return PageDTO.of(page, result);
    }

    @Override
    public void updateHidden(Long id, Boolean hidden) {
        Long userId = UserContext.getUser();
        if(userId==null){
            throw new BadRequestException("用户未登录");
        }
        this.lambdaUpdate().eq(InteractionQuestion::getId,id)
                .set(InteractionQuestion::getHidden,hidden)
                .update();
    }
}
