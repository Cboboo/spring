package org.myspringframework.core;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassName: ClassPathXmlApplicationContext
 * PackageName: org.myspringframework.core
 * Description:
 * MySpringFrameWork框架应用上下文实现类
 *
 * @Author CuiBo
 * @Create 2023/8/14 17:56
 * @Version 1.0
 */
public class ClassPathXmlApplicationContext implements ApplicationContext {

    private Map<String, Object> singletonObjects = new HashMap<>();

    /**
     * MySpring上下文构造器 完成以下任务：
     * 1.解析xml文档，获取bean定义信息
     * 2.创建相应的bean对象，并放入map容器
     *
     * @param configLocation xml文档路径
     */
    public ClassPathXmlApplicationContext(String configLocation) {
        try {
            //1.解析xml文档
            SAXReader reader = new SAXReader();
            InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(configLocation);
            Document document = reader.read(is);
            //获取xml文档的节点列表
            List<Node> nodes = document.selectNodes("//bean");
            //循环遍历节点列表
            for (Node node :
                    nodes) {
                //向下转型以便进一步解析
                Element beanElt = (Element) node;
                //获取bean的id和class信息
                String id = beanElt.attributeValue("id");
                String className = beanElt.attributeValue("class");
                //2.使用反射创建对象 - 此时属性值为空
                Class<?> clazz = Class.forName(className);
                Constructor<?> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                Object bean = constructor.newInstance();
                //将对象存入map集合
                singletonObjects.put(id, bean);
            }

            //3.再次遍历节点列表，获取属性标签
            for (Node node :
                    nodes) {
                //向下转型以便进一步解析
                Element beanElt = (Element) node;
                //获取bean的id和class信息
                String id = beanElt.attributeValue("id");
                String className = beanElt.attributeValue("class");

                //获取每个bean标签内部的property标签
                List<Element> properties = beanElt.elements("property");
                //循环遍历property标签
                for (Element e :
                        properties) {
                    //获取property标签的name属性（对应类中属性名）
                    String name = e.attributeValue("name");
                    //获取property标签的value或ref属性（对应类中属性要注入的值）
                    String value = e.attributeValue("value");
                    String ref = e.attributeValue("ref");
                    //根据name属性的值获取相应set方法
                    String methodName = "set" + name.toUpperCase().charAt(0) + name.substring(1);
                    Class<?> clazz = Class.forName(className);
                    //获取set方法需要属性的Class类型 首先根据配置文件中的name获取类中对应属性field 再使用field.getType()获取类型
                    Field field = clazz.getDeclaredField(name);
                    Method setMethod = clazz.getDeclaredMethod(methodName, field.getType());

                    //执行set方法进行依赖注入
                    if (value != null) {
                        //简单类型set注入
                        //临时变量 用于存放属性的注入值
                        Object actualValue = null;
                        //获取不带包名的属性类型字符串
                        String propertyTypeSimpleName = field.getType().getSimpleName();
                        switch (propertyTypeSimpleName) {
                            case "byte":
                                actualValue = Byte.parseByte(value);
                                break;
                            case "Byte":
                                actualValue = Byte.valueOf(value);
                                break;
                            case "short":
                                actualValue = Short.parseShort(value);
                                break;
                            case "Short":
                                actualValue = Short.valueOf(value);
                                break;
                            case "int":
                                actualValue = Integer.parseInt(value);
                                break;
                            case "Integer":
                                actualValue = Integer.valueOf(value);
                                break;
                            case "long":
                                actualValue = Long.parseLong(value);
                                break;
                            case "Long":
                                actualValue = Long.valueOf(value);
                                break;
                            case "double":
                                actualValue = Double.parseDouble(value);
                                break;
                            case "Double":
                                actualValue = Double.valueOf(value);
                                break;
                            case "boolean":
                                actualValue = Boolean.parseBoolean(value);
                                break;
                            case "Boolean":
                                actualValue = Boolean.valueOf(value);
                                break;
                            case "char":
                                actualValue = value.charAt(0);
                                break;
                            case "Character":
                                actualValue = Character.valueOf(value.charAt(0));
                                break;
                            case "String":
                                actualValue = value;
                                break;
                            default:
                                throw new RuntimeException("unsupported simple types");
                        }
                        setMethod.invoke(singletonObjects.get(id), actualValue);
                    }
                    if (ref != null) {
                        //非简单类型set注入
                        setMethod.invoke(singletonObjects.get(id), singletonObjects.get(ref));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object getBean(String beanName) {
        return singletonObjects.get(beanName);
    }
}
