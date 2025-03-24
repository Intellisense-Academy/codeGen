package com.mcode.llp.codegen;

import com.mcode.llp.codegen.initializer.Initializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CodeGenApplicationTest {

    @Mock
    private Initializer initializer;

    @Mock
    private ApplicationArguments args;

    @InjectMocks
    private CodeGenApplication codeGenApplication;

    @Test
    void testRun_ShouldCallInitializerMethods() {
        // Run the application runner
        codeGenApplication.run(args);

        // Verify that both initialization methods are called exactly once
        verify(initializer, times(1)).superUserInitialize();
        verify(initializer, times(1)).permissionInitialize();
    }
}
