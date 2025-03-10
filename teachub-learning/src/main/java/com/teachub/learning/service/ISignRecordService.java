package com.teachub.learning.service;


import com.teachub.learning.domain.vo.SignResultVO;

import java.util.List;

public interface ISignRecordService {

    SignResultVO addSignRecord();

    List<Integer> querySignRecord();
}
