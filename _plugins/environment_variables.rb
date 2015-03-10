# Plugin to add environment variables to the `site` object in Liquid templates

module Jekyll

	class EnvironmentVariablesGenerator < Generator

		def generate(site)
			site.config['ifluxserverurl'] = ENV['IFLUX_SERVER_URL'] || 'https://iflux.herokuapp.com'
			# Add other environment variables to `site.config` here...
		end

	end

end