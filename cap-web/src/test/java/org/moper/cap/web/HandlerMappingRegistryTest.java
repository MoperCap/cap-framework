package org.moper.cap.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.moper.cap.web.annotation.controller.Controller;
import org.moper.cap.web.annotation.controller.RestController;
import org.moper.cap.web.annotation.mapping.DeleteMapping;
import org.moper.cap.web.annotation.mapping.GetMapping;
import org.moper.cap.web.annotation.mapping.PostMapping;
import org.moper.cap.web.annotation.mapping.PutMapping;
import org.moper.cap.web.annotation.request.PathVariable;
import org.moper.cap.web.annotation.request.RequestBody;
import org.moper.cap.web.http.HttpMethod;
import org.moper.cap.web.mapping.HandlerMapping;
import org.moper.cap.web.mapping.registry.HandlerMappingRegistry;

import java.lang.annotation.Annotation;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link HandlerMappingRegistry}.
 */
class HandlerMappingRegistryTest {

    private HandlerMappingRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new HandlerMappingRegistry();
    }

    // ──────────────── Stub BeanContainer ────────────────

    /**
     * Minimal stub that returns a pre-configured set of controllers.
     */
    static class StubBeanContainer implements org.moper.cap.bean.container.BeanContainer {

        private final Map<String, Object> beans;

        StubBeanContainer(Map<String, Object> beans) {
            this.beans = beans;
        }

        @Override
        public Map<String, Object> getBeansWithAnnotation(
                Class<? extends Annotation> annotationType) {
            Map<String, Object> result = new LinkedHashMap<>();
            beans.forEach((name, bean) -> {
                if (bean.getClass().isAnnotationPresent(annotationType)) {
                    result.put(name, bean);
                }
            });
            return result;
        }

        // ── Unused stubs ──
        @Override public Object getBean(String beanName) { return beans.get(beanName); }
        @Override public <T> T getBean(String beanName, Class<T> requiredType) { return null; }
        @Override public <T> T getBean(Class<T> requiredType) { return null; }
        @Override public boolean containsBean(String n) { return false; }
        @Override public boolean containsBeanDefinition(String n) { return false; }
        @Override public org.moper.cap.bean.definition.BeanDefinition getBeanDefinition(String n) { return null; }
        @Override public String[] getBeanDefinitionNames() { return new String[0]; }
        @Override public int getBeanDefinitionCount() { return 0; }
        @Override public String[] getBeanNamesForType(Class<?> t) { return new String[0]; }
        @Override public String[] getBeanNamesForAnnotation(Class<? extends Annotation> a) { return new String[0]; }
        @Override public <T> Map<String, T> getBeansOfType(Class<T> t) { return Map.of(); }
        @Override public boolean isSingleton(String n) { return true; }
        @Override public boolean isPrototype(String n) { return false; }
        @Override public boolean isTypeMatch(String n, Class<?> t) { return false; }
        @Override public Class<?> getType(String n) { return null; }
        @Override public String[] getAliases(String n) { return new String[0]; }
        @Override public <A extends Annotation> A findAnnotationOnBean(String n, Class<A> t) { return null; }
        @Override public void registerBeanDefinition(org.moper.cap.bean.definition.BeanDefinition bd) {}
        @Override public void removeBeanDefinition(String n) {}
        @Override public void registerAlias(String n, String a) {}
        @Override public void removeAlias(String a) {}
        @Override public void registerSingleton(String n, Object o) {}
        @Override public boolean isBeanNameInUse(String n) { return false; }
        @Override public void addBeanInterceptor(org.moper.cap.bean.interceptor.BeanInterceptor i) {}
        @Override public List<org.moper.cap.bean.interceptor.BeanInterceptor> getBeanInterceptors() { return List.of(); }
        @Override public int getBeanInterceptorCount() { return 0; }
        @Override public void preInstantiateSingletons() {}
        @Override public void destroyBean(String n) {}
        @Override public void destroySingletons() {}
    }

    // ──────────────── Test controllers ────────────────

    @RestController
    static class UserController {

        @GetMapping("/users")
        public String listUsers() { return "users"; }

        @GetMapping("/users/{id}")
        public String getUser(@PathVariable("id") Long id) { return "user"; }

        @PostMapping("/users")
        public String createUser(@RequestBody String body) { return "created"; }

        @DeleteMapping("/users/{id}")
        public void deleteUser(@PathVariable("id") Long id) {}
    }

    @Controller
    static class ArticleController {

        @GetMapping("/articles")
        public String listArticles() { return "articles"; }

        @PutMapping("/articles/{id}")
        public String updateArticle(@PathVariable("id") Long id,
                                    @RequestBody String body) { return "updated"; }
    }

    // ──────────────── Tests ────────────────

    @Test
    void register_scansRestControllerMethods() {
        StubBeanContainer container = new StubBeanContainer(
                Map.of("userController", new UserController()));
        registry.register(container);

        assertFalse(registry.getAllMappings().isEmpty());
    }

    @Test
    void register_scansControllerMethods() {
        StubBeanContainer container = new StubBeanContainer(
                Map.of("articleController", new ArticleController()));
        registry.register(container);

        assertFalse(registry.getAllMappings().isEmpty());
    }

    @Test
    void findMapping_exactPath_returnsMatch() {
        StubBeanContainer container = new StubBeanContainer(
                Map.of("userController", new UserController()));
        registry.register(container);

        Optional<HandlerMapping> mapping = registry.findMapping("/users", HttpMethod.GET);
        assertTrue(mapping.isPresent());
        assertEquals("/users", mapping.get().path());
        assertEquals(HttpMethod.GET, mapping.get().httpMethod());
    }

    @Test
    void findMapping_pathWithVariable_returnsMatch() {
        StubBeanContainer container = new StubBeanContainer(
                Map.of("userController", new UserController()));
        registry.register(container);

        Optional<HandlerMapping> mapping = registry.findMapping("/users/42", HttpMethod.GET);
        assertTrue(mapping.isPresent());
        assertEquals("/users/{id}", mapping.get().path());
    }

    @Test
    void findMapping_wrongMethod_returnsEmpty() {
        StubBeanContainer container = new StubBeanContainer(
                Map.of("userController", new UserController()));
        registry.register(container);

        Optional<HandlerMapping> mapping = registry.findMapping("/users", HttpMethod.DELETE);
        assertFalse(mapping.isPresent());
    }

    @Test
    void findMapping_unknownPath_returnsEmpty() {
        StubBeanContainer container = new StubBeanContainer(
                Map.of("userController", new UserController()));
        registry.register(container);

        Optional<HandlerMapping> mapping = registry.findMapping("/unknown", HttpMethod.GET);
        assertFalse(mapping.isPresent());
    }

    @Test
    void findMapping_postMethod_returnsMatch() {
        StubBeanContainer container = new StubBeanContainer(
                Map.of("userController", new UserController()));
        registry.register(container);

        Optional<HandlerMapping> mapping = registry.findMapping("/users", HttpMethod.POST);
        assertTrue(mapping.isPresent());
        assertEquals(HttpMethod.POST, mapping.get().httpMethod());
    }

    @Test
    void findMapping_deleteWithPathVariable_returnsMatch() {
        StubBeanContainer container = new StubBeanContainer(
                Map.of("userController", new UserController()));
        registry.register(container);

        Optional<HandlerMapping> mapping = registry.findMapping("/users/99", HttpMethod.DELETE);
        assertTrue(mapping.isPresent());
        assertEquals(HttpMethod.DELETE, mapping.get().httpMethod());
    }

    @Test
    void findMapping_controllerAnnotation_returnsMatch() {
        StubBeanContainer container = new StubBeanContainer(
                Map.of("articleController", new ArticleController()));
        registry.register(container);

        Optional<HandlerMapping> mapping = registry.findMapping("/articles", HttpMethod.GET);
        assertTrue(mapping.isPresent());
    }

    @Test
    void findMapping_putWithPathVariable_returnsMatch() {
        StubBeanContainer container = new StubBeanContainer(
                Map.of("articleController", new ArticleController()));
        registry.register(container);

        Optional<HandlerMapping> mapping = registry.findMapping("/articles/5", HttpMethod.PUT);
        assertTrue(mapping.isPresent());
        assertEquals("/articles/{id}", mapping.get().path());
    }

    @Test
    void getAllMappings_returnsUnmodifiableList() {
        StubBeanContainer container = new StubBeanContainer(
                Map.of("userController", new UserController()));
        registry.register(container);

        List<HandlerMapping> mappings = registry.getAllMappings();
        assertThrows(UnsupportedOperationException.class, () -> mappings.add(null));
    }

    @Test
    void register_multipleControllers_registersAllMappings() {
        StubBeanContainer container = new StubBeanContainer(Map.of(
                "userController", new UserController(),
                "articleController", new ArticleController()));
        registry.register(container);

        // UserController has 4 methods, ArticleController has 2 methods
        assertTrue(registry.getAllMappings().size() >= 6);
    }

    @Test
    void pathVariableNames_extractedCorrectly() {
        StubBeanContainer container = new StubBeanContainer(
                Map.of("userController", new UserController()));
        registry.register(container);

        Optional<HandlerMapping> mapping = registry.findMapping("/users/42", HttpMethod.GET);
        assertTrue(mapping.isPresent());
        assertTrue(mapping.get().pathVariableNames().contains("id"));
    }
}
