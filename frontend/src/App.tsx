import { Container, Typography } from '@mui/material';

/**
 * Root component. Routing (React Router), authentication (Keycloak) and data fetching
 * (TanStack Query) are added by the first user stories that need them.
 */
export function App() {
  return (
    <Container component="main" maxWidth="lg" sx={{ py: 4 }}>
      <Typography variant="h4" component="h1">
        Plateforme SIGL
      </Typography>
    </Container>
  );
}
