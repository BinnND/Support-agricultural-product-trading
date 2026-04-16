class UserAuthManager:
    def __init__(self, user_info):
        self.user_info = user_info  # Dictionary containing user information

    def authenticate(self, username, password):
        # Logic to authenticate user
        if self.user_info.get(username) == password:
            return True
        return False

    def get_user_role(self, username):
        # Logic to retrieve user role
        return self.user_info.get(username, {}).get('role', 'guest')

    def is_authorized(self, username, required_role):
        user_role = self.get_user_role(username)
        return user_role == required_role

    def route_user(self, username):
        # Logic for role-based routing
        user_role = self.get_user_role(username)
        if user_role == 'admin':
            return '/admin_dashboard'
        elif user_role == 'trader':
            return '/trader_dashboard'
        elif user_role == 'buyer':
            return '/buyer_dashboard'
        else:
            return '/login'  # Default route for unauthorized users

