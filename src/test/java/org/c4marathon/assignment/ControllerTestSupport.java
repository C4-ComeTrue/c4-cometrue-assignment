package org.c4marathon.assignment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.c4marathon.assignment.member.presentation.MemberController;
import org.c4marathon.assignment.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {
        MemberController.class,
})
public class ControllerTestSupport {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    protected MemberService memberService;

}
