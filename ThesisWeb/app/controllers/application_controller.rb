class ApplicationController < ActionController::Base
  # Prevent CSRF attacks by raising an exception.
  # For APIs, you may want to use :null_session instead.
  protect_from_forgery with: :exception

=begin
  rescue_from CanCan::AccessDenied do |exception|
    if user_signed_in?
      redirect_to root_url, :alert => exception.message
    else
      redirect_to new_user_session_path, :alert => exception.message
    end
  end
=end

  rescue_from Faraday::ConnectionFailed do |exception|
    redirect_to errors_offline_path
  end

  # Overcome "ActiveModel::ForbiddenAttributesError" When Using CanCan in Rails 4
=begin
  before_action do
    resource = controller_name.singularize.to_sym
    method = "#{resource}_params"
    params[resource] &&= send(method) if respond_to?(method, true)
  end
=end

end
