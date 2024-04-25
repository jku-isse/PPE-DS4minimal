package at.jku.isse.designspace.core.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import at.jku.isse.designspace.core.events.ElementCreate;

public class Factory {

    static Constructor evaluatorConstructor=null;
    static public void setEvaluator(Class evaluatorClass)  {
        try {
            evaluatorConstructor=evaluatorClass.getConstructor(InstanceType.class, String.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("cannot find proper constructor for derived property evaluator");
        }
    }
    static public Evaluator createEvaluator(InstanceType instanceType, String rule) {
        try {
            return (Evaluator)evaluatorConstructor.newInstance(instanceType, rule);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    static HashMap<String, Constructor> elementConstructors=new HashMap<>();
    static public Element reconstructElement(Workspace workspace, ElementCreate elementCreate) {
        try {
            Constructor constructor = elementConstructors.get(elementCreate.className());
            if (constructor==null) {
                Class<?> clazz = Class.forName("at.jku.isse.designspace."+ elementCreate.className());
                constructor = clazz.getDeclaredConstructor(Workspace.class, ElementCreate.class);
                constructor.setAccessible(true);
                elementConstructors.put(elementCreate.className(), constructor);
            }

            if (constructor==null)
                return new Instance(workspace, elementCreate);
            else
                return (Element)constructor.newInstance(workspace, elementCreate);
        } catch (Exception e) {
            throw new IllegalArgumentException("cannot construct instance "+ elementCreate.elementId()+ " with type "+ elementCreate.instanceTypeId());
        }
    }
}
