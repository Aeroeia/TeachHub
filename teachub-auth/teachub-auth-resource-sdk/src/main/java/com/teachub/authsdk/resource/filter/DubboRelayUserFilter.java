package com.teachub.authsdk.resource.filter;

import com.teachub.auth.common.constants.JwtConstants;
import com.teachub.common.utils.UserContext;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

@Activate(group = CommonConstants.CONSUMER)
public class DubboRelayUserFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Long userId = UserContext.getUser();
        if (userId != null) {
            RpcContext.getContext().setAttachment(JwtConstants.USER_HEADER, userId.toString());
        }
        return invoker.invoke(invocation);
    }
}
