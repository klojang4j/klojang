package org.klojang.util;

import java.util.Map;
import nl.naturalis.common.invoke.BeanWriter;

/**
 * Utility class that lets you quickly convert a request parameter map (typically a JAX-RS {@code
 * MultiValuedMap} or a {@code Map<String, String[]>}) to a JavaBean.
 *
 * @author Ayco Holleman
 * @param <T> The type of the JavaBean
 */
public class RequestBeanifier<T> {

  private final BeanWriter<T> writer;

  /**
   * Creates a {@code RequestBeanifier} for the specified JavaBean type.
   *
   * @param beanClass The class of the JavaBean
   */
  public RequestBeanifier(Class<T> beanClass) {
    this.writer = BeanWriter.getTolerantWriter(beanClass);
  }

  /**
   * Populates the specified bean with request parameter values.
   *
   * @param bean The bean to populate
   * @param params The request parameters. Although the provided map would typically be a JAX-RS
   *     {@code MultiValuedMap} or a {@code Map<String, String[]>}, this method imposes no such
   *     restrictions.
   * @return The populated bean
   * @throws Throwable An exception arising from the dynamic invocation of the bean's setters
   */
  public T beanify(T bean, Map<String, ?> params) throws Throwable {
    writer.copy(params, bean);
    return bean;
  }
}
