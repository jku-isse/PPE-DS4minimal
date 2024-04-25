package at.jku.isse.designspace.jama.ui;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import at.jku.isse.designspace.artifactconnector.core.model.BaseElementType;
import at.jku.isse.designspace.jama.model.JamaBaseElementType;
import at.jku.isse.designspace.jama.service.IJamaService.JamaIdentifiers;
import at.jku.isse.designspace.jama.service.JamaService;

//@RestController
@CrossOrigin(origins = "*")//to allow incoming calls from other ports
//@ConditionalOnExpression(value = "not(${jama.mock:false}) and ${jama.enabled:false}")
public class ConnectorWebController {

	@Autowired
	private JamaService js;
	
	@GetMapping("/jamaconnector")
	public String viewMainConnectorPage(Model model) {
		// fetch all jama items known, sort by type
		List<SimpleItem> items = js.getAllKnownJamaItems().stream()
				.filter(inst -> (Boolean)inst.getPropertyAsValue(BaseElementType.FULLY_FETCHED)==true )
				.map(inst -> new SimpleItem((String) inst.getPropertyAsValueOrElse("id", () -> "unknown id"),
						(String) inst.getPropertyAsValueOrElse(BaseElementType.KEY, () -> "unknown document key"),
						(String) inst.name(),
						(String) inst.getPropertyAsValueOrElse(JamaBaseElementType.ITEM_TYPE_SHORT, () -> "unknown item type"),
						(String) inst.getPropertyAsValueOrElse(JamaBaseElementType.STATUS, () -> "unknown status"),
						 inst.id().toString()) )
				.sorted(SimpleItem.comparator)
				.collect(Collectors.toList());
		
		model.addAttribute("allitems", items);
		return "index";
	}
	
	@GetMapping("/jamaconnector/fetchItem")
	public String fetchNewItem(Model model) {
		ItemIdentifier itemId = new ItemIdentifier();
		model.addAttribute("itemidentifier", itemId);
		return "fetchnew";
	}
	
	@PostMapping("/jamaconnector/fetchItem")
	public String fetchItem(@ModelAttribute("itemidentifier") ItemIdentifier itemIdentifier, BindingResult result, Model model) {
		try {
			JamaIdentifiers idType = JamaIdentifiers.valueOf(itemIdentifier.getIdType());
			js.forceRefetch(itemIdentifier.getId(), idType);
		} catch (Exception e) {
			
		}		
		return "redirect:/jamaconnector";
	}
	
	@GetMapping("/jamaconnector/fetchItem/{id}")
	public String fetchItemViaId(@RequestParam("id") String id) {
		js.forceRefetch(id, JamaIdentifiers.JamaItemId);
		return "redirect:/jamaconnector";
	}
	
}
