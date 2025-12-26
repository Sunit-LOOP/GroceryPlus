# GroceryPlus Admin Panel Web App

## Overview
The GroceryPlus Admin Panel is a web-based interface for managing the e-commerce platform. It connects to the PHP backend API to perform administrative tasks like managing products, orders, users, and viewing analytics.

## Features
- **Dashboard**: Overview of key metrics and recent activity
- **Product Management**: Add, edit, delete products and categories
- **Order Management**: View and update order status
- **User Management**: Manage customer accounts
- **Analytics**: View sales reports and trends
- **Settings**: Configure system preferences

## Setup Instructions

### Prerequisites
- PHP backend server running (see main project README)
- Web browser with JavaScript enabled
- Internet connection for API calls

### Installation
1. Ensure the PHP backend is running on `http://YOUR_SERVER_IP/groceryplus/api`
2. Open `admin-website/index.html` in a web browser
3. Login with admin credentials (default: admin@groceryplus.com / admin123)

### API Configuration
- Base URL: `http://YOUR_SERVER_IP/groceryplus/api/`
- Authentication: Bearer token (obtained via login)
- CORS: Ensure backend allows requests from web app domain

## API Endpoints Used
- `POST /auth` - Admin login
- `GET /analytics` - Dashboard metrics
- `GET/POST/PUT/DELETE /products` - Product management
- `GET/PUT /orders` - Order management
- `GET/POST/PUT/DELETE /users` - User management
- `GET /categories` - Category management

## Usage
1. **Login**: Enter admin credentials
2. **Navigate**: Use the sidebar to access different sections
3. **Manage Data**: Use forms and tables to view/edit data
4. **Logout**: Click logout to end session

## Development
- **Backend**: PHP (server-side admin interface)
- **Frontend**: HTML/CSS/JavaScript (client-side interactions)
- **API Integration**: PHP connects to the same API endpoints as mobile app
- **Security**: Session-based authentication with API tokens

## Troubleshooting
- **API Connection**: Check backend server is running
- **CORS Errors**: Update PHP backend CORS headers
- **Login Issues**: Verify admin credentials in database
- **Data Not Loading**: Check API responses in browser dev tools

## File Structure
```
admin-website/
├── index.html          # Main admin interface
├── products.html       # Product management
├── orders.html         # Order management
├── users.html          # User management
├── categories.html     # Category management
├── analytics.html      # Analytics dashboard
├── css/
│   └── style.css       # Main styles
├── js/
│   ├── config.js       # API configuration
│   ├── auth.js         # Authentication
│   ├── dashboard.js    # Dashboard logic
│   ├── products.js     # Product management
│   ├── orders.js       # Order management
│   └── users.js        # User management
├── README.md           # This documentation
└── assets/
    └── images/         # Admin interface images
```

## Contributing
- Add new features by extending the HTML/JS
- Follow API documentation for new endpoints
- Test thoroughly with backend API

For API documentation, see `GroceryPlus_API_Documentation.md` in the root directory.