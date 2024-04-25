package at.jku.isse.designspace.rule.arl.repair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.core.model.InstanceType;
import at.jku.isse.designspace.rule.arl.repair.changepropagation.Change;


public class ConcreteRepairValueGenerator {
    private Map<String,Set> values;
    private static ConcreteRepairValueGenerator singleton = null;

    private ConcreteRepairValueGenerator() {
        this.values = new HashMap<>();
    }

    /**
     * Implemented singleton so generation of concrete values is not executed repeatedly for the same workspace
     * @return single instance of the class
     */
    public static ConcreteRepairValueGenerator getInstance(){
        if(singleton==null)
            singleton = new ConcreteRepairValueGenerator();
        return singleton;
    }

    /**
     * Returns a set of values for concrete repair options that are instances of a class
     * @param objectSet set of objects from where possible values may be extracted
     * @param c class that defines the instance type required
     * @return set of values
     */
    public Set getAllValues(Set<Object> objectSet, Class c) {
        Set storedValues = this.values.get(c.toString());
        if(storedValues != null && !storedValues.isEmpty())
            return storedValues;
        else {
            Set values = new HashSet<>();
            for (Object o : objectSet) {
                if (o instanceof Instance) {
                    Instance i = (Instance) o;
                    for (String propertyName :  i.getPropertyNames()) {
                        Object value = i.getPropertyAsValue(propertyName);
                        if (c.isInstance(value)) {
                            if(value instanceof String) {
                                if (!((String) value).trim().isEmpty())
                                    values.add(value);
                            }else{
                                values.add(value);
                            }
                        }
                    }
                } else if (o instanceof Change) {
                    Change change = (Change) o;
                    if (c.isInstance(change.getValue())) {
                        values.add(change.getValue());
                        Object value = change.getValue();
                        if(value instanceof String) {
                            if (!((String) value).trim().isEmpty())
                                values.add(value);
                        }else{
                            values.add(value);
                        }
                    }
                }
            }
            this.values.put(c.toString(),values);
            return values;
        }
    }

    /**
     * Returns a set of values for concrete repair options that are instances of a class based on a property
     * @param objectSet set of objects from where possible values may be extracted
     * @param c class that defines the instance type required
     * @param property property to look for in instances
     * @return set of values
     */
    public Set getAllValues(Set<Object> objectSet, String property, Class c) {
        Set storedValues = this.values.get(c.toString() + property);
        if (storedValues != null && !storedValues.isEmpty())
            return storedValues;
        else {
            Set values = new HashSet<>();
            for (Object o : objectSet) {
                if (o instanceof Instance) {
                    Instance i = (Instance) o;
                    if (i.hasProperty(property) && c.isInstance(i.getPropertyAsValue(property))) {
                        Object value = i.getPropertyAsValue(property);
                        if (value instanceof String) {
                            if (!((String) value).trim().isEmpty())
                                values.add(value);
                        } else {
                            values.add(value);
                        }

                    }

                } else if (o instanceof Change) {
                    Change change = (Change) o;
                    if (change.getProperty().equals(property) && c.isInstance(change.getValue())) {
                        Object value = change.getValue();
                        if (value instanceof String) {
                            if (!((String) value).trim().isEmpty())
                                values.add(value);
                        } else {
                            values.add(value);
                        }
                    }
                }
            }
            this.values.put(c + property, values);
            return values;

        }

    }

    /**
     * Returns a set of values for concrete repair options of the Instance class
     * @param objectSet set of objects from where possible values may be extracted
     * @param type instance type used for getting the instances
     * @return set of values
     */
    public Set getAllInstancesAsValues(Set<Object> objectSet, InstanceType type) {
        Set storedValues = this.values.get(type.toString()+type.workspace);
        if(storedValues != null && !storedValues.isEmpty())
            return storedValues;
        else {
            Set values = new HashSet<>();
            for (Object o : objectSet) {
                if(o instanceof Instance){
                    Instance i1 = (Instance) o;
                    if(i1.getInstanceType().equals(type))
                        values.add(i1);
                }else if(o instanceof Change) {
                    Change change = (Change) o;
                    Instance i1 = (Instance) change.getElement();
                    if(i1.getInstanceType().equals(type))
                        values.add(i1);
                    if(change.getValue() instanceof Instance) {
                        Instance i2 = (Instance) change.getValue();
                        if(i2.getInstanceType().equals(type))
                            values.add(i2);
                    }
                }
            }
            this.values.put(type.toString()+type.workspace,values);
            return values;
        }
    }
}
