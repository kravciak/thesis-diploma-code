require 'test_helper'

class ErrorsControllerTest < ActionController::TestCase
  test "should get offline" do
    get :offline
    assert_response :success
  end

end
