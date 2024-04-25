package at.jku.isse.designspace.rule.arl.repair.changepropagation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import at.jku.isse.designspace.core.model.Instance;
import at.jku.isse.designspace.rule.arl.repair.Operator;

public class Change {
    private Object element;
    private String property;
    private Object value;
    private Operator operator;
    private ChangeType type;
    private List<Change> subChanges;

    public Change(Object object, String property, Operator operator, Object value, ChangeType type) {
        this.element = object;
        this.property = property;
        this.value = value;
        this.operator = operator;
        this.type = type;
        this.subChanges = new ArrayList<>();
    }
    public Change(Object object, String property, Operator operator, Object value) {
        this.element = object;
        this.property = property;
        this.value = value;
        this.operator = operator;
        this.type = ChangeType.NEUTRAL;
        this.subChanges = new ArrayList<>();
    }

    public void addSubChange(Change d){
        subChanges.add(d);
    }

    public List<Change> getSubChanges() {
        return subChanges;
    }

    public Object getElement() {
        return element;
    }


    public void setType(ChangeType type) {
        this.type = type;
    }

    public ChangeType getType() {
        return type;
    }

    public Operator getOperator() {
        return operator;
    }

    public String getProperty() {
        return property;
    }

    public Object getValue() {
        return value;
    }

    public void setElement(Object element) {
        this.element = element;
    }

    @Override
    public String toString() {
        Instance i = null;
        String typeString = "";
        if(getElement() != null) {
            if(getElement() instanceof Instance){
                i = (Instance) getElement();
                typeString = i.getInstanceType().toString();
            }
            else
                return toStringNoInstance();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(getElement() == null ?  "" : i.toString().substring(9).replace(typeString,""));
        sb.append("(");
        sb.append(getElement() == null ?  "" : typeString.substring(14,typeString.length()-6)+"Type");
        sb.append(")");
        sb.append(".");
        sb.append(getProperty() == null ?  "" : getProperty());
        sb.append(" ");
        sb.append(getOperator().toString());
        sb.append(" ");
        sb.append(getValue() == null ?  "?" : getValue());
        for (Change c: subChanges){
            sb.append(", <");
            sb.append(c);
            sb.append(" >");
        }
        return sb.toString();
    }

    private String toStringNoInstance() {
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        sb.append(getElement() == null ?  "" : getElement());
        sb.append(".");
        sb.append(getProperty() == null ?  "" : getProperty());
        sb.append(" ");
        sb.append(getOperator().toString());
        sb.append(" ");
        sb.append(getValue() == null ?  "?" : getValue());
        sb.append(" >");
        for (Change c: subChanges){
            sb.append(", <");
            sb.append(c);
            sb.append(" >");
        }
        return sb.toString();
    }


    public enum ChangeType{
        NEUTRAL,
        NEGATIVE,
        POSITIVE
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Change change = (Change) o;
        return Objects.equals(this.hashCode(),change.hashCode());
    }

    public boolean isConflict(Change c){
        if(c.getValue()==null || this.getValue()==null) return false;
        if(this.equals(c)) return false;
        if(c.getOperator().equals(Operator.ADD) && this.getOperator().equals(Operator.REMOVE)) { // check if other change is ADD and this change is REMOVE the same element
            Instance i = (Instance) this.getValue();
            Instance i2 = (Instance) c.getValue();
            if(i.equals(i2)) return true;
        }
        if(c.getOperator().equals(Operator.REMOVE) && this.getOperator().equals(Operator.ADD)) { // check if other change is REMOVE and this change is ADD the same element
            Instance i = (Instance) this.getValue();
            Instance i2 = (Instance) c.getValue();
            if(i.equals(i2)) return true;
        }
        if(this.getProperty()!= null && c.getProperty() != null) { // check if is the same repair actions with different values
            Instance i = (Instance) this.getElement();
            Instance i2 = (Instance) c.getElement();
            if (i.equals(i2) && this.getProperty().equals(c.getProperty()))
                return true;
        }
        if(this.getElement() instanceof Instance){ // check if other change action is Add or Remove and this is Modify the same element
            Instance i = (Instance) this.getElement();
            if(c.getValue() instanceof Instance) {
                Instance i2 = (Instance) c.getValue();
                if(i.equals(i2)) return true;
            }
        }
        if(this.getValue() instanceof Instance){ // check if this change action is Add or Remove and other is Modify the same element
            Instance i = (Instance) this.getValue();
            if(c.getElement() instanceof Instance) {
                Instance i2 = (Instance) c.getElement();
                if(i.equals(i2)) return true;
            }
        }

        return false;
    }



    @Override
    public int hashCode() {
        return Objects.hash(element, property, value);
    }
}
