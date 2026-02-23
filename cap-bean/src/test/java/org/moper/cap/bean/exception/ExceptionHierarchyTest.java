package org.moper.cap.bean.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("异常体系")
class ExceptionHierarchyTest {

    // ==================== 继承树验证 ====================

    @Nested
    @DisplayName("继承树正确性验证")
    class HierarchyValidation {

        @Test
        @DisplayName("BeanException 是 RuntimeException 的子类（非受检）")
        void beanException_isRuntimeException() {
            assertInstanceOf(RuntimeException.class, new BeanException("msg"));
        }

        @Test
        @DisplayName("BeanCreationException 是 BeanException 的子类")
        void beanCreationException_isBeanException() {
            assertInstanceOf(BeanException.class, new BeanCreationException("bean", "msg"));
        }

        @Test
        @DisplayName("BeanInitializationException 是 BeanCreationException 的子类")
        void beanInitializationException_isBeanCreationException() {
            assertInstanceOf(BeanCreationException.class,
                    new BeanInitializationException("bean", new RuntimeException()));
        }

        @Test
        @DisplayName("BeanInitializationException 是 BeanException 的子类（传递性）")
        void beanInitializationException_isBeanException() {
            assertInstanceOf(BeanException.class,
                    new BeanInitializationException("bean", new RuntimeException()));
        }

        @Test
        @DisplayName("BeanDestructionException 是 BeanException 的子类")
        void beanDestructionException_isBeanException() {
            assertInstanceOf(BeanException.class,
                    new BeanDestructionException("bean", new RuntimeException()));
        }

        @Test
        @DisplayName("BeanDestructionException 不是 BeanCreationException 的子类")
        void beanDestructionException_isNotBeanCreationException() {
            assertFalse(BeanCreationException.class.isAssignableFrom(BeanDestructionException.class));
        }

        @Test
        @DisplayName("BeanDefinitionException 是 BeanException 的子类")
        void beanDefinitionException_isBeanException() {
            assertInstanceOf(BeanException.class, new BeanDefinitionException("msg"));
        }

        @Test
        @DisplayName("BeanDefinitionStoreException 是 BeanDefinitionException 的子类")
        void beanDefinitionStoreException_isBeanDefinitionException() {
            assertInstanceOf(BeanDefinitionException.class, new BeanDefinitionStoreException("msg"));
        }

        @Test
        @DisplayName("NoSuchBeanDefinitionException 是 BeanDefinitionException 的子类")
        void noSuchBeanDefinitionException_isBeanDefinitionException() {
            assertInstanceOf(BeanDefinitionException.class, new NoSuchBeanDefinitionException("bean"));
        }

        @Test
        @DisplayName("NoUniqueBeanDefinitionException 是 NoSuchBeanDefinitionException 的子类")
        void noUniqueBeanDefinitionException_isNoSuchBeanDefinitionException() {
            assertInstanceOf(NoSuchBeanDefinitionException.class,
                    new NoUniqueBeanDefinitionException(String.class, "a", "b"));
        }

        @Test
        @DisplayName("BeanNotOfRequiredTypeException 是 BeanException 的子类")
        void beanNotOfRequiredTypeException_isBeanException() {
            assertInstanceOf(BeanException.class,
                    new BeanNotOfRequiredTypeException("bean", String.class, Integer.class));
        }

        @Test
        @DisplayName("所有异常均为非受检异常（RuntimeException 的子类）")
        void allExceptions_areUnchecked() {
            assertTrue(RuntimeException.class.isAssignableFrom(BeanException.class));
            assertTrue(RuntimeException.class.isAssignableFrom(BeanCreationException.class));
            assertTrue(RuntimeException.class.isAssignableFrom(BeanInitializationException.class));
            assertTrue(RuntimeException.class.isAssignableFrom(BeanDestructionException.class));
            assertTrue(RuntimeException.class.isAssignableFrom(BeanDefinitionException.class));
            assertTrue(RuntimeException.class.isAssignableFrom(BeanDefinitionStoreException.class));
            assertTrue(RuntimeException.class.isAssignableFrom(NoSuchBeanDefinitionException.class));
            assertTrue(RuntimeException.class.isAssignableFrom(NoUniqueBeanDefinitionException.class));
            assertTrue(RuntimeException.class.isAssignableFrom(BeanNotOfRequiredTypeException.class));
        }
    }

    // ==================== 异常消息与字段验证 ====================

    @Nested
    @DisplayName("异常消息与字段验证")
    class ExceptionFieldsValidation {

        @Test
        @DisplayName("BeanCreationException：beanName 被正确保存，消息包含 beanName")
        void beanCreationException_beanNamePreservedInMessage() {
            BeanCreationException ex = new BeanCreationException("myBean", "something went wrong");
            assertEquals("myBean", ex.getBeanName());
            assertTrue(ex.getMessage().contains("myBean"));
            assertTrue(ex.getMessage().contains("something went wrong"));
        }

        @Test
        @DisplayName("BeanCreationException：带 cause 的构造器正确保存 cause")
        void beanCreationException_causePreserved() {
            RuntimeException cause = new RuntimeException("root cause");
            BeanCreationException ex = new BeanCreationException("myBean", "msg", cause);
            assertSame(cause, ex.getCause());
        }

        @Test
        @DisplayName("BeanInitializationException：beanName 被正确保存，消息含 afterPropertiesSet")
        void beanInitializationException_beanNameAndMessage() {
            RuntimeException cause = new RuntimeException("init error");
            BeanInitializationException ex = new BeanInitializationException("myBean", cause);
            assertEquals("myBean", ex.getBeanName());
            assertTrue(ex.getMessage().contains("afterPropertiesSet"));
            assertSame(cause, ex.getCause());
        }

        @Test
        @DisplayName("BeanDestructionException：beanName 被正确保存，消息包含 beanName")
        void beanDestructionException_beanNamePreservedInMessage() {
            RuntimeException cause = new RuntimeException("destroy error");
            BeanDestructionException ex = new BeanDestructionException("myBean", cause);
            assertEquals("myBean", ex.getBeanName());
            assertTrue(ex.getMessage().contains("myBean"));
            assertSame(cause, ex.getCause());
        }

        @Test
        @DisplayName("NoSuchBeanDefinitionException(String)：消息包含 beanName")
        void noSuchBean_byName_messageContainsBeanName() {
            NoSuchBeanDefinitionException ex = new NoSuchBeanDefinitionException("targetBean");
            assertTrue(ex.getMessage().contains("targetBean"));
        }

        @Test
        @DisplayName("NoSuchBeanDefinitionException(Class)：消息包含类名")
        void noSuchBean_byType_messageContainsTypeName() {
            NoSuchBeanDefinitionException ex = new NoSuchBeanDefinitionException(String.class);
            assertTrue(ex.getMessage().contains(String.class.getName()));
        }

        @Test
        @DisplayName("NoUniqueBeanDefinitionException：getNumberOfBeansFound() 返回正确数量")
        void noUniqueBean_getNumberOfBeansFound_correct() {
            NoUniqueBeanDefinitionException ex =
                    new NoUniqueBeanDefinitionException(String.class, "beanA", "beanB", "beanC");
            assertEquals(3, ex.getNumberOfBeansFound());
        }

        @Test
        @DisplayName("NoUniqueBeanDefinitionException：消息包含所有候选 Bean 名称")
        void noUniqueBean_messageContainsAllCandidateNames() {
            NoUniqueBeanDefinitionException ex =
                    new NoUniqueBeanDefinitionException(String.class, "beanA", "beanB");
            assertTrue(ex.getMessage().contains("beanA"));
            assertTrue(ex.getMessage().contains("beanB"));
        }

        @Test
        @DisplayName("BeanNotOfRequiredTypeException：所有字段被正确保存")
        void beanNotOfRequiredType_allFieldsPreserved() {
            BeanNotOfRequiredTypeException ex =
                    new BeanNotOfRequiredTypeException("myBean", String.class, Integer.class);
            assertEquals("myBean", ex.getBeanName());
            assertEquals(String.class, ex.getRequiredType());
            assertEquals(Integer.class, ex.getActualType());
            assertTrue(ex.getMessage().contains("myBean"));
            assertTrue(ex.getMessage().contains(String.class.getName()));
            assertTrue(ex.getMessage().contains(Integer.class.getName()));
        }
    }

    // ==================== 异常作为父类型捕获 ====================

    @Nested
    @DisplayName("异常可作为父类型捕获")
    class CatchByParentType {

        @Test
        @DisplayName("BeanInitializationException 可被 BeanCreationException 捕获")
        void initializationException_caughtAsBeanCreationException() {
            assertDoesNotThrow(() -> {
                try {
                    throw new BeanInitializationException("bean", new RuntimeException());
                } catch (BeanCreationException e) {
                    // 正确捕获
                }
            });
        }

        @Test
        @DisplayName("NoUniqueBeanDefinitionException 可被 NoSuchBeanDefinitionException 捕获")
        void noUniqueException_caughtAsNoSuchBeanDefinitionException() {
            assertDoesNotThrow(() -> {
                try {
                    throw new NoUniqueBeanDefinitionException(String.class, "a", "b");
                } catch (NoSuchBeanDefinitionException e) {
                    // 正确捕获
                }
            });
        }

        @Test
        @DisplayName("所有 cap-bean 异常均可被 BeanException 统一捕获")
        void allExceptions_caughtAsBeanException() {
            assertDoesNotThrow(() -> {
                try { throw new BeanCreationException("b", "m"); } catch (BeanException e) {}
                try { throw new BeanInitializationException("b", new RuntimeException()); } catch (BeanException e) {}
                try { throw new BeanDestructionException("b", new RuntimeException()); } catch (BeanException e) {}
                try { throw new BeanDefinitionStoreException("m"); } catch (BeanException e) {}
                try { throw new NoSuchBeanDefinitionException("b"); } catch (BeanException e) {}
                try { throw new NoUniqueBeanDefinitionException(String.class, "a"); } catch (BeanException e) {}
                try { throw new BeanNotOfRequiredTypeException("b", String.class, Integer.class); } catch (BeanException e) {}
            });
        }
    }
}