class Ability
  include CanCan::Ability

  def initialize(user)
    # user ||= User.new

    # Guest
    unless user
    
    # Member (All roles)
    else
      can :describe, Api
      can :resend, Api
      can :event, Api

      can :create, Statement
      can [:read, :update, :destroy], Statement, :user_id => user.id
      can [:control, :clean, :export], Statement, :user_id => user.id

      # case user.role
      # when 'admin'
      #   can :manage, :all
      # end
      if user.email == 'kravciak@gmail.com'
        can :manage, :all
      end

    end

    # See the wiki for details:
    # https://github.com/ryanb/cancan/wiki/Defining-Abilities
  end
end