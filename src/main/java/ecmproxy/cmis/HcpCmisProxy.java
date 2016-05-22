package ecmproxy.cmis;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.ecm.api.AbstractCmisProxyServlet;
import com.sap.ecm.api.EcmService;
import com.sap.ecm.api.RepositoryOptions;
import com.sap.ecm.api.RepositoryOptions.Visibility;

public class HcpCmisProxy
extends AbstractCmisProxyServlet {

	private static final long serialVersionUID = 1L;

	private static Logger LOGGER = LoggerFactory.getLogger(HcpCmisProxy.class);

	@Override
	protected String getRepositoryKey() {
		return System.getProperty("CMIS_REPO_KEY");
	}

	@Override
	protected String getRepositoryUniqueName() {
		return System.getProperty("CMIS_REPO_NAME");
	}

	@Override
	public void init() {
		Object service = null;
		try {
			InitialContext context = new InitialContext();
			service = context.lookup("java:comp/env/CmisService");
		}
		catch (NamingException e) {
			LOGGER.error("Unable to look for CMIS service in JNDI context");
			throw new ExceptionInInitializerError(e);
		}
		if (service == null) {
			throw new ExceptionInInitializerError("CMIS service was null in JNDI context");
		}
		if (!(service instanceof EcmService)) {
			LOGGER.error("Expected CMIS service instance of type [" + EcmService.class.getName() +
				"] but got [" + service.getClass().getName() + "]");
			throw new ExceptionInInitializerError("Unexpected CMIS service instance type");
		}
		try {
			((EcmService) service).connect(getRepositoryUniqueName(), getRepositoryKey());
		}
		catch (CmisObjectNotFoundException e) {
			LOGGER.info("CMIS repository does not exist. Create one.");
			RepositoryOptions options = new RepositoryOptions();
			options.setUniqueName(getRepositoryUniqueName());
			options.setRepositoryKey(getRepositoryKey());
			options.setVisibility(Visibility.PROTECTED);
			((EcmService) service).createRepository(options);
			LOGGER.info("CMIS repository created");
		}
		LOGGER.info("CMIS proxy ready");
	}

}
