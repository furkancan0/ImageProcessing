package com.ImageProcessing;

import com.ImageProcessing.entity.Image;
import com.ImageProcessing.entity.User;
import com.ImageProcessing.repository.projection.ImageUserProjection;
import org.mockito.Mockito;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServiceTestHelper {
    private static final ProjectionFactory factory = new SpelAwareProxyProjectionFactory();
    public static final PageRequest pageable = PageRequest.of(0, 10);

    public static <T> T createImageProjection(boolean isDeleted, Class<T> type) {
        Map<String, Object> imageMap = new HashMap<>();
        imageMap.put("id", 10L);
        imageMap.put("name", "my image");
        imageMap.put("type", "png");
        imageMap.put("imageData", new byte[]{1, 2, 3});
        imageMap.put("imageDate", LocalDateTime.now());
        imageMap.put("user", createImageUserProjection());
        imageMap.put("deleted", isDeleted);
        return factory.createProjection(type, imageMap);
    }

    public static ImageUserProjection createImageUserProjection() {
        return factory.createProjection(
                ImageUserProjection.class,
                new HashMap<>() {{
                    put("id", 2L);
                    put("name", "Furkan");
                    put("email", "furkan@gmail.com");
                    put("isPrivateProfile", false);
                }});
    }

    public static void mockAuthenticatedUser() {;
        User applicationUser = new User();
        applicationUser.setId(1L);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(applicationUser);
    }

    public static Image createImage(boolean isDeleted){
        Image image = new Image();
        image.setId(10L);
        image.setDeleted(isDeleted);
        image.setImageData(new byte[]{1, 2, 3});
        image.setImageDate(LocalDateTime.now());
        return image;
    }
}
