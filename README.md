# OfficeSaga

OfficeSaga is a web-based office life game and work journal. It helps a user create a personal avatar, create avatars for colleagues, track projects and tasks, record collaboration and feelings about work, and use AI to generate task summaries for future reference.

The first version is planned as a single-user application with authentication. This keeps the product manageable while still allowing us to learn real-world security patterns and prepare the system for future multi-user support.

## Vision

OfficeSaga combines productivity and storytelling:

- Build your own office identity with a customizable avatar
- Create colleague profiles with roles and positions
- Track projects, tasks, and collaborators
- Record mood and reflection for each task or project
- Generate AI-written task summaries after work is completed

The long-term goal is to make work history feel more like an evolving story than a plain task list.

## Planned Tech Stack

### Frontend

- TypeScript
- React
- TSX

### Backend

- Java
- Spring Boot
- Spring Security

### Database

- MySQL

### Cloud / Platform

- Cloud deployment for frontend, backend, and database
- AI API integration for summary generation

## Core Functions

### 1. Authentication

- User registration
- User login and logout
- Protected routes and APIs
- Per-user data ownership

### 2. Avatar System

- Create your own avatar
- Create avatars for colleagues
- Store basic profile information such as name, role, and position
- Support future avatar customization and visual upgrades

### 3. Project and Task Tracking

- Create and manage projects
- Create tasks under a project
- Record task status and completion
- Link tasks to colleagues you worked with

### 4. Work Reflection

- Record how you feel about a task or project
- Save notes about progress, blockers, or collaboration
- Build a personal work history over time

### 5. AI Summary Generation

- Generate a summary after a task or project is completed
- Save the generated text for later review
- Allow the user to revise the summary before finalizing it

## Initial Product Scope

The initial version of OfficeSaga will focus on a single user per account:

- No shared team workspace yet
- No real-time collaboration yet
- No multi-user permissions system yet

This approach gives us a smaller and safer MVP while keeping the data model ready for future expansion.

## MVP Goals

The first milestone is:

1. A user can create an account and sign in.
2. A user can create their avatar.
3. A user can add colleagues and their positions.
4. A user can create projects and tasks.
5. A user can record collaborators and mood.
6. A user can complete a task and receive an AI-generated summary.

## Future Directions

- Richer avatar customization
- Game-style progression and achievements
- Relationship tracking with colleagues
- Timeline and story-based visualization
- Multi-user collaboration
- Cloud-based sync and deployment improvements
